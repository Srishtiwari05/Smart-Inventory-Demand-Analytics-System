from flask import Flask, jsonify
import joblib
import pandas as pd
import datetime
import os
import pymysql

app = Flask(__name__)

# Load the trained model
model_path = 'demand_model.pkl'
if os.path.exists(model_path):
    model = joblib.load(model_path)
    print(f"Loaded model from {model_path}")
else:
    print(f"WARNING: Model {model_path} not found. Run train.py first.")
    model = None


def get_real_features(product_id):
    """
    Query MySQL for actual recent sales of this product.
    Returns (lag_1_sales, ma_7) based on real order_items data.
    Falls back to (0.0, 0.0) if the product has no recent history.
    """
    connection = pymysql.connect(
        host='localhost',
        user='root',
        password='Bhanu@2205',
        database='smart_inventory',
        cursorclass=pymysql.cursors.DictCursor
    )
    try:
        with connection.cursor() as cursor:
            # Get daily sales quantity for this product over the last 8 days.
            # 8 days covers: yesterday (lag_1) + 7 days for the moving average.
            query = """
                SELECT DATE(o.order_date) AS sale_date, SUM(oi.quantity) AS daily_qty
                FROM orders o
                JOIN order_items oi ON o.id = oi.order_id
                WHERE oi.product_id = %s
                  AND o.order_date >= CURDATE() - INTERVAL 8 DAY
                GROUP BY DATE(o.order_date)
                ORDER BY sale_date DESC
            """
            cursor.execute(query, (product_id,))
            rows = cursor.fetchall()
    finally:
        connection.close()

    if not rows:
        # No sales history — use 0 as safe fallback
        return 0.0, 0.0

    # rows is ordered DESC (most recent first)
    # lag_1_sales = quantity sold yesterday (most recent day available)
    lag_1_sales = float(rows[0]['daily_qty'])

    # 7-day moving average = average of all rows (up to 7 days)
    last_7 = rows[:7]
    ma_7 = sum(float(r['daily_qty']) for r in last_7) / len(last_7)

    return lag_1_sales, round(ma_7, 2)


@app.route('/predict/<int:product_id>', methods=['GET'])
def predict_demand(product_id):
    if model is None:
        return jsonify({"error": "Model not trained. Run train.py first."}), 500

    try:
        today = datetime.datetime.now()
        day_of_week = today.weekday()
        month = today.month
        is_weekend = 1 if day_of_week >= 5 else 0

        # Phase 9: use real historical sales instead of mocked values
        lag_1_sales, ma_7 = get_real_features(product_id)
        print(f"[Phase 9] product_id={product_id} lag_1={lag_1_sales} ma_7={ma_7}")

        features = pd.DataFrame([{
            'product_id': product_id,
            'day_of_week': day_of_week,
            'month': month,
            'is_weekend': is_weekend,
            'lag_1_sales': lag_1_sales,
            '7_day_ma': ma_7
        }])

        daily_prediction = model.predict(features)[0]
        monthly_prediction = max(1, int(round(daily_prediction * 30)))

        return jsonify({
            "productId": product_id,
            "predictedDemand": monthly_prediction,
            "forecastPeriod": "next_30_days",
            "dailyDemandRate": round(daily_prediction, 2),
            "featuresUsed": {
                "lag_1_sales": lag_1_sales,
                "7_day_ma": ma_7
            }
        })

    except pymysql.Error as db_err:
        return jsonify({"error": f"Database error: {str(db_err)}"}), 500
    except Exception as e:
        return jsonify({"error": str(e)}), 500


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)

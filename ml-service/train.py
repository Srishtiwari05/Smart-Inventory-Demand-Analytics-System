import pymysql
import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestRegressor
from sklearn.metrics import mean_absolute_error, root_mean_squared_error
import joblib
import os

print("Connecting to MySQL...")
# Connect to MySQL
connection = pymysql.connect(
    host='localhost',
    user='root',
    password='Bhanu@2205',
    database='smart_inventory',
    cursorclass=pymysql.cursors.DictCursor
)

try:
    print("Extracting historical sales data...")
    query = """
    SELECT 
        DATE(o.order_date) as date,
        oi.product_id,
        SUM(oi.quantity) as daily_quantity
    FROM orders o
    JOIN order_items oi ON o.id = oi.order_id
    GROUP BY DATE(o.order_date), oi.product_id
    ORDER BY date ASC
    """
    cursor = connection.cursor()
    cursor.execute(query)
    rows = cursor.fetchall()
    df = pd.DataFrame(rows)
    
finally:
    connection.close()

if df.empty:
    print("Error: No data found in the database. Cannot train model.")
    exit(1)

print(f"Extracted {len(df)} records. Engineering features...")

# Ensure date is datetime
df['date'] = pd.to_datetime(df['date'])

# Feature Engineering
# Extract basic time features
df['day_of_week'] = df['date'].dt.dayofweek
df['month'] = df['date'].dt.month
df['is_weekend'] = df['day_of_week'].apply(lambda x: 1 if x >= 5 else 0)

# Sort by product and date to calculate lags
df = df.sort_values(by=['product_id', 'date']).reset_index(drop=True)

# Calculate Lag 1 (previous day sales) and 7-Day Moving Average
# We use fillna(0) instead of dropna() because the seed dataset is very small
df['lag_1_sales'] = df.groupby('product_id')['daily_quantity'].shift(1).fillna(0)
df['7_day_ma'] = df.groupby('product_id')['daily_quantity'].transform(lambda x: x.rolling(window=7, min_periods=1).mean()).fillna(0)

print("Data sample:")
print(df.head())

# Features and Target
features = ['product_id', 'day_of_week', 'month', 'is_weekend', 'lag_1_sales', '7_day_ma']
target = 'daily_quantity'

X = df[features]
y = df[target]

# Chronological split: train on earlier data, test on later data
# Since dataset might be small, let's just do a standard 80/20 train/test split without shuffling
print("Splitting data chronologically...")
split_idx = int(len(df) * 0.8)
X_train, X_test = X.iloc[:split_idx], X.iloc[split_idx:]
y_train, y_test = y.iloc[:split_idx], y.iloc[split_idx:]

if len(X_train) == 0 or len(X_test) == 0:
    print("Dataset too small for train/test split. Training on entire dataset for demonstration.")
    X_train, X_test = X, X
    y_train, y_test = y, y

print("Training RandomForestRegressor model...")
model = RandomForestRegressor(n_estimators=100, random_state=42)
model.fit(X_train, y_train)

print("Evaluating model...")
predictions = model.predict(X_test)

mae = mean_absolute_error(y_test, predictions)
rmse = root_mean_squared_error(y_test, predictions)

print(f"==============================")
print(f"Model Evaluation Metrics:")
print(f"MAE:  {mae:.4f}")
print(f"RMSE: {rmse:.4f}")
print(f"==============================")

# Save the model
model_path = 'demand_model.pkl'
joblib.dump(model, model_path)
print(f"Model saved successfully to {model_path}")

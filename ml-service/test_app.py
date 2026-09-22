import unittest
import json
import os
from app import app, model

class MLServiceTestCase(unittest.TestCase):

    def setUp(self):
        self.client = app.test_client()

    def test_health_endpoint(self):
        """Test that the /health endpoint responds with status UP."""
        response = self.client.get('/health')
        self.assertEqual(response.status_code, 200)
        data = json.loads(response.data)
        self.assertEqual(data.get("status"), "UP")
        self.assertEqual(data.get("service"), "ml-demand-prediction")
        self.assertIn("modelLoaded", data)

    def test_predict_endpoint_response_structure(self):
        """Test that /predict/<product_id> returns structured forecast JSON when model is loaded."""
        if model is None:
            self.skipTest("demand_model.pkl not trained/found; skipping inference test.")

        response = self.client.get('/predict/1')
        # Response should either succeed (200) or fail gracefully on DB error (500 with json message)
        self.assertIn(response.status_code, [200, 500])
        data = json.loads(response.data)
        if response.status_code == 200:
            self.assertEqual(data.get("productId"), 1)
            self.assertIn("predictedDemand", data)
            self.assertIn("forecastPeriod", data)
            self.assertIn("dailyDemandRate", data)
            self.assertIn("featuresUsed", data)

    def test_model_file_existence(self):
        """Verify demand_model.pkl is present in ml-service directory."""
        model_exists = os.path.exists('demand_model.pkl')
        self.assertTrue(model_exists, "Model file demand_model.pkl should be present")

if __name__ == '__main__':
    unittest.main()

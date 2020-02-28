import unittest
from sys import path

#path.append('../')
from app import app

HOME_URL = 'http://127.0.0.1:5000/'
BAD_URL = '{}/5'.format(HOME_URL)

class BasicTests(unittest.TestCase):

    def setUp(self):
        self.app = app.test_client()
        

    def test_alive(self):
        response = self.app.get(HOME_URL+"alive")
        self.assertEqual(response.status_code, 200)

    def test_hello(self):
        response = self.app.get(HOME_URL+"hello/alice")
        self.assertEqual(response.status_code, 200)

if __name__ == "__main__":
    unittest.main()

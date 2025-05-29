const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');
const axios = require('axios');

const app = express();
const PORT = process.env.PORT || 3001;
const DATA_SERVICE_URL = process.env.DATA_SERVICE_URL || 'http://localhost:3002';

// Middleware
app.use(cors());
app.use(bodyParser.json());

// Health check
app.get('/health', async (req, res) => {
  try {
    // Check if data service is available
    const dataServiceHealth = await axios.get(`${DATA_SERVICE_URL}/health`);
    
    if (dataServiceHealth.status === 200) {
      res.status(200).json({ status: 'healthy', dependencies: { dataService: 'healthy' } });
    } else {
      res.status(200).json({ status: 'degraded', dependencies: { dataService: 'unhealthy' } });
    }
  } catch (error) {
    res.status(200).json({ status: 'degraded', dependencies: { dataService: 'unavailable' } });
  }
});

// Create a new customer
app.post('/api/customers', async (req, res) => {
  try {
    const { name, alias, dob } = req.body;
    
    // Validate input
    if (!name || !alias || !dob) {
      return res.status(400).json({ error: 'Name, alias, and date of birth are required' });
    }
    
    // Validate date format
    if (isNaN(Date.parse(dob))) {
      return res.status(400).json({ error: 'Date of birth must be in a valid date format' });
    }
    
    // Forward request to data service
    const response = await axios.post(`${DATA_SERVICE_URL}/customers`, { name, alias, dob });
    
    res.status(201).json(response.data);
  } catch (error) {
    console.error('Error creating customer:', error.message);
    
    if (error.response) {
      // Forward error from data service
      res.status(error.response.status).json(error.response.data);
    } else {
      res.status(500).json({ error: 'Failed to create customer' });
    }
  }
});

// Get all customers
app.get('/api/customers', async (req, res) => {
  try {
    const response = await axios.get(`${DATA_SERVICE_URL}/customers`);
    res.json(response.data);
  } catch (error) {
    console.error('Error retrieving customers:', error.message);
    
    if (error.response) {
      res.status(error.response.status).json(error.response.data);
    } else {
      res.status(500).json({ error: 'Failed to retrieve customers' });
    }
  }
});

// Search for a customer
app.get('/api/customers/search', async (req, res) => {
  try {
    const { id, name, alias } = req.query;
    
    if (!id && !name && !alias) {
      return res.status(400).json({ error: 'At least one search parameter (id, name, or alias) is required' });
    }
    
    const response = await axios.get(`${DATA_SERVICE_URL}/customers/search`, {
      params: { id, name, alias }
    });
    
    res.json(response.data);
  } catch (error) {
    console.error('Error searching for customer:', error.message);
    
    if (error.response) {
      res.status(error.response.status).json(error.response.data);
    } else {
      res.status(500).json({ error: 'Failed to search for customer' });
    }
  }
});

// Update customer
app.put('/api/customers/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const { name, alias, dob } = req.body;
    
    if (!name && !alias && !dob) {
      return res.status(400).json({ error: 'At least one field to update is required' });
    }
    
    // Validate date format if provided
    if (dob && isNaN(Date.parse(dob))) {
      return res.status(400).json({ error: 'Date of birth must be in a valid date format' });
    }
    
    const response = await axios.put(`${DATA_SERVICE_URL}/customers/${id}`, { name, alias, dob });
    
    res.json(response.data);
  } catch (error) {
    console.error('Error updating customer:', error.message);
    
    if (error.response) {
      res.status(error.response.status).json(error.response.data);
    } else {
      res.status(500).json({ error: 'Failed to update customer' });
    }
  }
});

// Delete customer
app.delete('/api/customers/:id', async (req, res) => {
  try {
    const { id } = req.params;
    
    const response = await axios.delete(`${DATA_SERVICE_URL}/customers/${id}`);
    
    res.json(response.data);
  } catch (error) {
    console.error('Error deleting customer:', error.message);
    
    if (error.response) {
      res.status(error.response.status).json(error.response.data);
    } else {
      res.status(500).json({ error: 'Failed to delete customer' });
    }
  }
});

// Start server
app.listen(PORT, () => {
  console.log(`API Service running on port ${PORT}`);
  console.log(`Connected to Data Service at ${DATA_SERVICE_URL}`);
});

module.exports = app; // For testing

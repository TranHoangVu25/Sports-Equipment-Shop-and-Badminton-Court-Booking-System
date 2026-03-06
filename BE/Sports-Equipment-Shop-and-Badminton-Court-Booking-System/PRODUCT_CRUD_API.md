# Product CRUD API Documentation

## Overview

Complete CRUD (Create, Read, Update, Delete) API for Product and ProductImage management, including batch operations.

## API Endpoints Summary

| Method | Endpoint | Description | Status Code |
|--------|----------|-------------|------------|
| POST | `/api/v1/products` | Create single product | 201 |
| POST | `/api/v1/products/batch` | Create multiple products | 201 |
| GET | `/api/v1/products` | Get all products | 200 |
| GET | `/api/v1/products/{id}` | Get product by ID | 200 |
| PUT | `/api/v1/products/{id}` | Update product | 200 |
| DELETE | `/api/v1/products/{id}` | Delete single product | 200 |
| DELETE | `/api/v1/products/batch` | Delete multiple products | 200 |

---

## CREATE Operations

### 1. Create Single Product

**Endpoint:**
```
POST /api/v1/products
```

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{
    "id": "24189",
    "name": "Set băng chặn mồ hôi Yonex 11510-2",
    "price": "99000",
    "priceCurrency": "VND",
    "description": "Set băng chặn mồ hôi...",
    "quantity": "294",
    "status": "còn hàng",
    "images": [
      "https://shopvnb.com/uploads/gallery/image1.webp",
      "https://shopvnb.com/uploads/gallery/image2.webp"
    ],
    "mainCategory": "Phụ Kiện Cầu Lông",
    "subCategory": "Băng chặn mồ hôi"
  }'
```

**Response (201 Created):**
```json
{
  "status": 201,
  "error": null,
  "timestamp": "2024-03-06T10:30:45.123",
  "message": "Created successfully",
  "data": {
    "productId": 24189,
    "name": "Set băng chặn mồ hôi Yonex 11510-2",
    "price": 99000,
    "priceCurrency": "VND",
    "description": "Set băng chặn mồ hôi...",
    "quantity": 294,
    "status": "còn hàng",
    "mainCategory": "Phụ Kiện Cầu Lông",
    "subCategory": "Băng chặn mồ hôi",
    "datePublished": "2024-03-06T10:30:45.123",
    "updatedAt": "2024-03-06T10:30:45.123",
    "productImages": [
      {
        "productId": 24189,
        "imageUrl": "https://shopvnb.com/uploads/gallery/image1.webp"
      },
      {
        "productId": 24189,
        "imageUrl": "https://shopvnb.com/uploads/gallery/image2.webp"
      }
    ]
  }
}
```

### 2. Create Multiple Products (Batch)

**Endpoint:**
```
POST /api/v1/products/batch
```

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/products/batch \
  -H "Content-Type: application/json" \
  -d '[
    {
      "id": "24189",
      "name": "Product 1",
      "price": "99000",
      "priceCurrency": "VND",
      "description": "Description 1",
      "quantity": "294",
      "status": "còn hàng",
      "images": ["https://example.com/img1.webp"],
      "mainCategory": "Category 1",
      "subCategory": "SubCategory 1"
    },
    {
      "id": "24190",
      "name": "Product 2",
      "price": "150000",
      "priceCurrency": "VND",
      "description": "Description 2",
      "quantity": "100",
      "status": "còn hàng",
      "images": ["https://example.com/img2.webp", "https://example.com/img3.webp"],
      "mainCategory": "Category 2",
      "subCategory": "SubCategory 2"
    }
  ]'
```

**Response (201 Created):**
```json
{
  "status": 201,
  "error": null,
  "timestamp": "2024-03-06T10:30:45.123",
  "message": "Created successfully",
  "data": {
    "total": 2,
    "success": 2,
    "failed": 0,
    "products": [
      {
        "productId": 24189,
        "name": "Product 1",
        ...
      },
      {
        "productId": 24190,
        "name": "Product 2",
        ...
      }
    ],
    "errors": []
  }
}
```

---

## READ Operations

### 1. Get All Products

**Endpoint:**
```
GET /api/v1/products
```

**Request:**
```bash
curl -X GET http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json"
```

**Response (200 OK):**
```json
{
  "status": 200,
  "error": null,
  "timestamp": "2024-03-06T10:30:45.123",
  "message": "Fetched successfully",
  "data": [
    {
      "productId": 24189,
      "name": "Set băng chặn mồ hôi Yonex 11510-2",
      "price": 99000,
      "priceCurrency": "VND",
      "description": "Description...",
      "quantity": 294,
      "status": "còn hàng",
      "mainCategory": "Phụ Kiện Cầu Lông",
      "subCategory": "Băng chặn mồ hôi",
      "datePublished": "2024-03-06T10:30:45.123",
      "updatedAt": "2024-03-06T10:30:45.123",
      "productImages": [...]
    },
    {
      "productId": 24190,
      "name": "Product 2",
      ...
    }
  ]
}
```

### 2. Get Product by ID

**Endpoint:**
```
GET /api/v1/products/{id}
```

**Request:**
```bash
curl -X GET http://localhost:8080/api/v1/products/24189 \
  -H "Content-Type: application/json"
```

**Response (200 OK):**
```json
{
  "status": 200,
  "error": null,
  "timestamp": "2024-03-06T10:30:45.123",
  "message": "Fetched successfully",
  "data": {
    "productId": 24189,
    "name": "Set băng chặn mồ hôi Yonex 11510-2",
    "price": 99000,
    "priceCurrency": "VND",
    "description": "Description...",
    "quantity": 294,
    "status": "còn hàng",
    "mainCategory": "Phụ Kiện Cầu Lông",
    "subCategory": "Băng chặn mồ hôi",
    "datePublished": "2024-03-06T10:30:45.123",
    "updatedAt": "2024-03-06T10:30:45.123",
    "productImages": [
      {
        "productId": 24189,
        "imageUrl": "https://shopvnb.com/uploads/gallery/image1.webp"
      },
      {
        "productId": 24189,
        "imageUrl": "https://shopvnb.com/uploads/gallery/image2.webp"
      }
    ]
  }
}
```

**Error Response (404 Not Found):**
```json
{
  "status": 404,
  "error": "Product with ID 99999 not found",
  "timestamp": "2024-03-06T10:30:45.123",
  "message": "Operation failed",
  "data": null
}
```

---

## UPDATE Operations

### 1. Update Product by ID

**Endpoint:**
```
PUT /api/v1/products/{id}
```

**Description:** Updates all product fields and images. If images are provided, old images are deleted and new ones are created.

**Request:**
```bash
curl -X PUT http://localhost:8080/api/v1/products/24189 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Product Name",
    "price": "120000",
    "priceCurrency": "VND",
    "description": "Updated description",
    "quantity": "500",
    "status": "hết hàng",
    "images": [
      "https://shopvnb.com/uploads/gallery/new-image1.webp",
      "https://shopvnb.com/uploads/gallery/new-image2.webp",
      "https://shopvnb.com/uploads/gallery/new-image3.webp"
    ],
    "mainCategory": "Phụ Kiện Cầu Lông",
    "subCategory": "Băng chặn mồ hôi"
  }'
```

**Response (200 OK):**
```json
{
  "status": 200,
  "error": null,
  "timestamp": "2024-03-06T10:30:45.123",
  "message": "Updated successfully",
  "data": {
    "productId": 24189,
    "name": "Updated Product Name",
    "price": 120000,
    "priceCurrency": "VND",
    "description": "Updated description",
    "quantity": 500,
    "status": "hết hàng",
    "mainCategory": "Phụ Kiện Cầu Lông",
    "subCategory": "Băng chặn mồ hôi",
    "datePublished": "2024-03-06T10:30:45.123",
    "updatedAt": "2024-03-06T11:15:30.456",
    "productImages": [
      {
        "productId": 24189,
        "imageUrl": "https://shopvnb.com/uploads/gallery/new-image1.webp"
      },
      {
        "productId": 24189,
        "imageUrl": "https://shopvnb.com/uploads/gallery/new-image2.webp"
      },
      {
        "productId": 24189,
        "imageUrl": "https://shopvnb.com/uploads/gallery/new-image3.webp"
      }
    ]
  }
}
```

**Error Response (404 Not Found):**
```json
{
  "status": 404,
  "error": "Product with ID 99999 not found",
  "timestamp": "2024-03-06T10:30:45.123",
  "message": "Operation failed",
  "data": null
}
```

---

## DELETE Operations

### 1. Delete Single Product

**Endpoint:**
```
DELETE /api/v1/products/{id}
```

**Description:** Deletes a product and all its associated images (cascade delete).

**Request:**
```bash
curl -X DELETE http://localhost:8080/api/v1/products/24189 \
  -H "Content-Type: application/json"
```

**Response (200 OK):**
```json
{
  "status": 200,
  "error": null,
  "timestamp": "2024-03-06T10:30:45.123",
  "message": "Deleted successfully",
  "data": null
}
```

**Error Response (404 Not Found):**
```json
{
  "status": 404,
  "error": "Product with ID 99999 not found",
  "timestamp": "2024-03-06T10:30:45.123",
  "message": "Operation failed",
  "data": null
}
```

### 2. Delete Multiple Products (Batch)

**Endpoint:**
```
DELETE /api/v1/products/batch
```

**Description:** Deletes multiple products in batch. Returns detailed statistics about success/failure.

**Request:**
```bash
curl -X DELETE http://localhost:8080/api/v1/products/batch \
  -H "Content-Type: application/json" \
  -d '[24189, 24190, 24191]'
```

**Response (200 OK) - All Success:**
```json
{
  "status": 200,
  "error": null,
  "timestamp": "2024-03-06T10:30:45.123",
  "message": "Deleted successfully",
  "data": {
    "total": 3,
    "success": 3,
    "failed": 0,
    "products": [],
    "errors": []
  }
}
```

**Response (200 OK) - Partial Success:**
```json
{
  "status": 200,
  "error": null,
  "timestamp": "2024-03-06T10:30:45.123",
  "message": "Deleted successfully",
  "data": {
    "total": 3,
    "success": 2,
    "failed": 1,
    "products": [],
    "errors": [
      "Product 3 (ID: 99999): Not found"
    ]
  }
}
```

---

## Request/Response Fields

### Product Object

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `id` | Long | No | Product ID (auto-generated if not provided) |
| `productId` | Long | No | Product ID (in response) |
| `name` | String | Yes | Product name (max 255) |
| `price` | BigDecimal | Yes | Product price |
| `priceCurrency` | String | Yes | Currency (e.g., "VND") |
| `description` | String | No | Product description |
| `quantity` | Integer | Yes | Stock quantity |
| `status` | String | Yes | Product status |
| `images` | Array | Yes | Image URLs |
| `mainCategory` | String | Yes | Main category |
| `subCategory` | String | Yes | Sub category |
| `datePublished` | DateTime | No | Publication date (auto-set) |
| `updatedAt` | DateTime | No | Last update date (auto-set) |
| `productImages` | Array | No | List of ProductImage objects |

### BatchProductResponse Object

| Field | Type | Description |
|-------|------|-------------|
| `total` | Integer | Total number of items processed |
| `success` | Integer | Number of successful operations |
| `failed` | Integer | Number of failed operations |
| `products` | Array | Created/Updated products (empty for delete) |
| `errors` | Array | Error messages for failed items |

---

## Common Error Responses

### 400 Bad Request
```json
{
  "status": 400,
  "error": "Product name is required",
  "timestamp": "2024-03-06T10:30:45.123",
  "message": "Operation failed",
  "data": null
}
```

### 404 Not Found
```json
{
  "status": 404,
  "error": "Product with ID 99999 not found",
  "timestamp": "2024-03-06T10:30:45.123",
  "message": "Operation failed",
  "data": null
}
```

### 500 Internal Server Error
```json
{
  "status": 500,
  "error": "Database connection error",
  "timestamp": "2024-03-06T10:30:45.123",
  "message": "Operation failed",
  "data": null
}
```

---

## Validation Rules

### Product Creation/Update
- **name**: Required, non-blank (max 255 chars)
- **price**: Required, numeric, > 0
- **priceCurrency**: Required, non-blank
- **quantity**: Required, integer >= 0
- **status**: Required, non-blank
- **mainCategory**: Required, non-blank
- **subCategory**: Required, non-blank
- **images**: Required, at least one URL

### Batch Delete
- **productIds**: Array of valid product IDs
- Invalid IDs will be reported in errors, not throw exception

---

## Cascading Behavior

### Product Deletion
When a product is deleted:
1. All associated ProductImage records are automatically deleted (cascade delete)
2. Product record is removed from database
3. Transaction is rolled back if any error occurs

### Image Updates
When a product is updated with new images:
1. Old images are deleted
2. New images are created
3. All operations are transactional

---

## Batch Operations

### Batch Delete Behavior
- **Atomic per item**: Each deletion is independent
- **Partial failure**: If one fails, others continue
- **Detailed reporting**: Errors include item index and product ID
- **Return success count**: Shows how many were deleted

### Example Batch Delete with Mixed Results
```bash
curl -X DELETE http://localhost:8080/api/v1/products/batch \
  -H "Content-Type: application/json" \
  -d '[24189, 99999, 24190]'
```

Response:
```json
{
  "status": 200,
  "error": null,
  "timestamp": "2024-03-06T10:30:45.123",
  "message": "Deleted successfully",
  "data": {
    "total": 3,
    "success": 2,
    "failed": 1,
    "products": [],
    "errors": [
      "Product 2 (ID: 99999): Not found"
    ]
  }
}
```

---

## Best Practices

1. **Always validate input** before sending requests
2. **Use IDs returned from creation** for subsequent operations
3. **Handle batch errors gracefully** - check the errors array
4. **Check response status code** for quick error detection
5. **Update all fields** when updating a product
6. **Verify images** are accessible before creation
7. **Use batch operations** for bulk inserts/deletes
8. **Monitor updatedAt timestamp** to verify updates
9. **Implement retry logic** for transient failures
10. **Log error messages** for debugging

---

## Testing with Postman

### 1. Create Product
- **Method**: POST
- **URL**: `http://localhost:8080/api/v1/products`
- **Body**: JSON (product data)

### 2. Get All Products
- **Method**: GET
- **URL**: `http://localhost:8080/api/v1/products`

### 3. Get Product by ID
- **Method**: GET
- **URL**: `http://localhost:8080/api/v1/products/24189`

### 4. Update Product
- **Method**: PUT
- **URL**: `http://localhost:8080/api/v1/products/24189`
- **Body**: JSON (updated product data)

### 5. Delete Product
- **Method**: DELETE
- **URL**: `http://localhost:8080/api/v1/products/24189`

### 6. Delete Batch
- **Method**: DELETE
- **URL**: `http://localhost:8080/api/v1/products/batch`
- **Body**: JSON array of IDs `[24189, 24190, 24191]`

---

## Related Documentation

- [BaseResponse API Documentation](BASE_RESPONSE_API.md)
- [Product API Updated](PRODUCT_API_UPDATED.md)
- [Implementation Summary](IMPLEMENTATION_SUMMARY.md)


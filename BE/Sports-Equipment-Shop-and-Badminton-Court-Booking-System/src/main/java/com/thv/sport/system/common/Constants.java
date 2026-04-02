package com.thv.sport.system.common;

import lombok.Getter;

import java.time.LocalTime;

/**
 * Constants class containing all API endpoints and application-wide constants
 */
@Getter
public class Constants {

    // Base API path
    public static final String API_BASE = "/api/v1";

    // Public files path
    public static final String PUBLIC_FILES = "/public-files";

    /**
     * API Paths for all endpoints
     */
    public static class ApiPath {

        // Product Management APIs
        public static final String API_PRODUCT = API_BASE + "/products";
        public static final String API_PRODUCT_IMAGE = API_BASE + "/product-images";

        // Booking APIs
        public static final String API_BOOKING = API_BASE + "/bookings";
        public static final String API_BOOKING_DETAIL = API_BASE + "/booking-details";
        public static final String API_BOOKING_PAYMENT = API_BASE + "/booking-payments";

        // Court Management APIs
        public static final String API_COURT = API_BASE + "/courts";
        public static final String API_COURT_CENTER = API_BASE + "/court-centers";

        // Order Management APIs
        public static final String API_ORDER = API_BASE + "/orders";
        public static final String API_ORDER_ITEM = API_BASE + "/order-items";

        // Cart APIs
        public static final String API_CART = API_BASE + "/cart";
        public static final String API_CART_ITEM = API_BASE + "/cart-items";

        // User Management APIs
        public static final String API_USER = API_BASE + "/users";
        public static final String API_ACCOUNT = API_BASE + "/account";
        public static final String API_ADDRESS = API_BASE + "/addresses";
        public static final String API_AUTH = API_BASE + "/auth";

        // Payment APIs
        public static final String API_PAYMENT = API_BASE + "/payments";

        // Promotion APIs
        public static final String API_PROMOTION = API_BASE + "/promotions";

        // Report and Analytics APIs
        public static final String API_REPORT = API_BASE + "/reports";
        public static final String API_ANALYTICS = API_BASE + "/analytics";
        public static final String API_DASHBOARD = API_BASE + "/dashboard";

        public static final String API_HOME = API_BASE + "/home-page";

        public static final String API_SEARCH = API_BASE + "/search";
        public static final String API_WEBHOOK = API_BASE + "/webhook";


    }

    /**
     * HTTP Status Messages
     */
    public static class StatusMessages {
        public static final String SUCCESS = "Success";
        public static final String ERROR = "Error";
        public static final String VALIDATION_ERROR = "Validation Error";
        public static final String NOT_FOUND = "Not Found";
        public static final String UNAUTHORIZED = "Unauthorized";
        public static final String FORBIDDEN = "Forbidden";
        public static final String CONFLICT = "Conflict";
    }

    /**
     * Entity Status Constants
     */
    public static class EntityStatus {
        public static final String ACTIVE = "active";
        public static final String INACTIVE = "inactive";
        public static final String DELETED = "deleted";
        public static final String AVAILABLE = "còn hàng";
        public static final String OUT_OF_STOCK = "hết hàng";
    }

    /**
     * Order Status Constants
     */
    public static class OrderStatus {
        public static final String PENDING = "pending";
        public static final String PROCESSING = "processing";
        public static final String SHIPPED = "shipped";
        public static final String DELIVERED = "delivered";
        public static final String CANCELLED = "cancelled";
        public static final String REFUNDED = "refunded";
        public static final String SUCCESS = "success";

    }

    /**
     * Booking Status Constants
     */
    public static class BookingStatus {
        public static final String PENDING = "pending";
        public static final String CONFIRMED = "confirmed";
        public static final String CANCELLED = "cancelled";
        public static final String COMPLETED = "completed";
    }

    /**
     * Payment Status Constants
     */
    public static class PaymentStatus {
        public static final String PENDING = "pending";
        public static final String COMPLETED = "completed";
        public static final String FAILED = "failed";
        public static final String REFUNDED = "refunded";
    }

    /**
     * Currency Constants
     */
    public static class Currency {
        public static final String VND = "VND";
        public static final String USD = "USD";
        public static final String EUR = "EUR";
    }

    /**
     * Pagination Constants
     */
    public static class Pagination {
        public static final int DEFAULT_PAGE_SIZE = 20;
        public static final int DEFAULT_PAGE_NUMBER = 0;
        public static final int MAX_PAGE_SIZE = 100;
    }

    /**
     * File Upload Constants
     */
    public static class FileUpload {
        public static final String UPLOAD_DIR = "uploads/";
        public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
        public static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".webp"};
    }

    /**
     * Validation Constants
     */
    public static class Validation {
        public static final int NAME_MIN_LENGTH = 3;
        public static final int NAME_MAX_LENGTH = 255;
        public static final int DESCRIPTION_MAX_LENGTH = 1000;
        public static final int PASSWORD_MIN_LENGTH = 8;
        public static final int PASSWORD_MAX_LENGTH = 50;
    }

    public static class CheckoutMethod {
        public static final String COD = "COD";
        public static final String STRIPE = "STRIPE";
    }

    public static class Role {
        public static final String USER = "user";
        public static final String ADMIN = "admin";
    }

    public static class SizeType {
        // APPAREL, SHOE, RACKET, NONE
        public static final String APPAREL = "APPAREL"; // áo, quần, váy
        public static final String SHOE = "SHOE";       // giày
        public static final String RACKET = "RACKET";   // vợt
        public static final String NONE = "NONE";       // balo, túi, phụ kiện, máy...
    }

    public static class ExchangeRate {
        // 1JPY = 170 VND
        public static final long VND_PER_JPY = 170;
    }

    public static class CourtStatus {
        public static final String ACTIVE = "ACTIVE";
        public static final String INACTIVE = "INACTIVE";
    }

    public static class ActiveStatus {
        public static final Boolean TRUE_VALUE = Boolean.TRUE;
        public static final Boolean FALSE_VALUE = Boolean.FALSE;
    }
    //NORMAL → giá bình thường
    //PEAK → giờ cao điểm (18h–22h)
    //HOLIDAY → ngày lễ
    public static class CourtRuleType {
        public static final String NORMAL = "NORMAL";
        public static final String PEAK = "PEAK";
        public static final String HOLIDAY = "HOLIDAY";
    }

    public static class DeleteFlag {
        public static final Integer TRUE = 1;
        public static final Integer FALSE = 0;
    }

    public static class CourtTimeSlotDefault {
        public static final LocalTime START_TIME = LocalTime.of(5, 0);
        public static final LocalTime END_TIME = LocalTime.of(23, 59);
    }
}


package ru.cashbox.android.common

// --- 1xx Informational ---

// --- 1xx Informational ---
/** `100 Continue` (HTTP/1.1 - RFC 2616)  */
const val CONTINUE = 100
/** `101 Switching Protocols` (HTTP/1.1 - RFC 2616) */
const val SWITCHING_PROTOCOLS = 101
/** `102 Processing` (WebDAV - RFC 2518)  */
const val PROCESSING = 102

// --- 2xx Success ---

// --- 2xx Success ---
/** `200 OK` (HTTP/1.0 - RFC 1945)  */
const val OK = 200
/** `201 Created` (HTTP/1.0 - RFC 1945)  */
const val CREATED = 201
/** `202 Accepted` (HTTP/1.0 - RFC 1945)  */
const val ACCEPTED = 202
/** `203 Non Authoritative Information` (HTTP/1.1 - RFC 2616)  */
const val NON_AUTHORITATIVE_INFORMATION = 203
/** `204 No Content` (HTTP/1.0 - RFC 1945)  */
const val NO_CONTENT = 204
/** `205 Reset Content` (HTTP/1.1 - RFC 2616)  */
const val RESET_CONTENT = 205
/** `206 Partial Content` (HTTP/1.1 - RFC 2616)  */
const val PARTIAL_CONTENT = 206
/**
 * `207 Multi-Status` (WebDAV - RFC 2518)
 * or
 * `207 Partial Update OK` (HTTP/1.1 - draft-ietf-http-v11-spec-rev-01?)
 */
const val MULTI_STATUS = 207

// --- 3xx Redirection ---

// --- 3xx Redirection ---
/** `300 Mutliple Choices` (HTTP/1.1 - RFC 2616)  */
const val MULTIPLE_CHOICES = 300
/** `301 Moved Permanently` (HTTP/1.0 - RFC 1945)  */
const val MOVED_PERMANENTLY = 301
/** `302 Moved Temporarily` (Sometimes `Found`) (HTTP/1.0 - RFC 1945)  */
const val MOVED_TEMPORARILY = 302
/** `303 See Other` (HTTP/1.1 - RFC 2616)  */
const val SEE_OTHER = 303
/** `304 Not Modified` (HTTP/1.0 - RFC 1945)  */
const val NOT_MODIFIED = 304
/** `305 Use Proxy` (HTTP/1.1 - RFC 2616)  */
const val USE_PROXY = 305
/** `307 Temporary Redirect` (HTTP/1.1 - RFC 2616)  */
const val TEMPORARY_REDIRECT = 307

// --- 4xx Client Error ---

// --- 4xx Client Error ---
/** `400 Bad Request` (HTTP/1.1 - RFC 2616)  */
const val BAD_REQUEST = 400
/** `401 Unauthorized` (HTTP/1.0 - RFC 1945)  */
const val UNAUTHORIZED = 401
/** `402 Payment Required` (HTTP/1.1 - RFC 2616)  */
const val PAYMENT_REQUIRED = 402
/** `403 Forbidden` (HTTP/1.0 - RFC 1945)  */
const val FORBIDDEN = 403
/** `404 Not Found` (HTTP/1.0 - RFC 1945)  */
const val NOT_FOUND = 404
/** `405 Method Not Allowed` (HTTP/1.1 - RFC 2616)  */
const val METHOD_NOT_ALLOWED = 405
/** `406 Not Acceptable` (HTTP/1.1 - RFC 2616)  */
const val NOT_ACCEPTABLE = 406
/** `407 Proxy Authentication Required` (HTTP/1.1 - RFC 2616) */
const val PROXY_AUTHENTICATION_REQUIRED = 407
/** `408 Request Timeout` (HTTP/1.1 - RFC 2616)  */
const val REQUEST_TIMEOUT = 408
/** `409 Conflict` (HTTP/1.1 - RFC 2616)  */
const val CONFLICT = 409
/** `410 Gone` (HTTP/1.1 - RFC 2616)  */
const val GONE = 410
/** `411 Length Required` (HTTP/1.1 - RFC 2616)  */
const val LENGTH_REQUIRED = 411
/** `412 Precondition Failed` (HTTP/1.1 - RFC 2616)  */
const val PRECONDITION_FAILED = 412
/** `413 Request Entity Too Large` (HTTP/1.1 - RFC 2616)  */
const val REQUEST_TOO_LONG = 413
/** `414 Request-URI Too Long` (HTTP/1.1 - RFC 2616)  */
const val REQUEST_URI_TOO_LONG = 414
/** `415 Unsupported Media Type` (HTTP/1.1 - RFC 2616)  */
const val UNSUPPORTED_MEDIA_TYPE = 415
/** `416 Requested Range Not Satisfiable` (HTTP/1.1 - RFC 2616)  */
const val REQUESTED_RANGE_NOT_SATISFIABLE = 416
/** `417 Expectation Failed` (HTTP/1.1 - RFC 2616)  */
const val EXPECTATION_FAILED = 417

/**
 * Static constant for a 418 error.
 * {@code 418 Unprocessable Entity} (WebDAV drafts?)
 * or {@code 418 Reauthentication Required} (HTTP/1.1 drafts?)
 */
// not used
// public static final int UNPROCESSABLE_ENTITY = 418;

/**
 * Static constant for a 418 error.
 * `418 Unprocessable Entity` (WebDAV drafts?)
 * or `418 Reauthentication Required` (HTTP/1.1 drafts?)
 */
// not used
// public static final int UNPROCESSABLE_ENTITY = 418;
/**
 * Static constant for a 419 error.
 * `419 Insufficient Space on Resource`
 * (WebDAV - draft-ietf-webdav-protocol-05?)
 * or `419 Proxy Reauthentication Required`
 * (HTTP/1.1 drafts?)
 */
const val INSUFFICIENT_SPACE_ON_RESOURCE = 419
/**
 * Static constant for a 420 error.
 * `420 Method Failure`
 * (WebDAV - draft-ietf-webdav-protocol-05?)
 */
const val METHOD_FAILURE = 420
/** `422 Unprocessable Entity` (WebDAV - RFC 2518)  */
const val UNPROCESSABLE_ENTITY = 422
/** `423 Locked` (WebDAV - RFC 2518)  */
const val LOCKED = 423
/** `424 Failed Dependency` (WebDAV - RFC 2518)  */
const val FAILED_DEPENDENCY = 424

// --- 5xx Server Error ---

// --- 5xx Server Error ---
/** `500 Server Error` (HTTP/1.0 - RFC 1945)  */
const val INTERNAL_SERVER_ERROR = 500
/** `501 Not Implemented` (HTTP/1.0 - RFC 1945)  */
const val NOT_IMPLEMENTED = 501
/** `502 Bad Gateway` (HTTP/1.0 - RFC 1945)  */
const val BAD_GATEWAY = 502
/** `503 Service Unavailable` (HTTP/1.0 - RFC 1945)  */
const val SERVICE_UNAVAILABLE = 503
/** `504 Gateway Timeout` (HTTP/1.1 - RFC 2616)  */
const val GATEWAY_TIMEOUT = 504
/** `505 HTTP Version Not Supported` (HTTP/1.1 - RFC 2616)  */
const val HTTP_VERSION_NOT_SUPPORTED = 505

/** `507 Insufficient Storage` (WebDAV - RFC 2518)  */
const val INSUFFICIENT_STORAGE = 507
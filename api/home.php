<?php
// ============================================================
//  api/home.php — JSON API for the Android app's Home tab
//  Upload this file to: /bethesdabiblicalinstitute/api/home.php
//
//  Reuses your EXISTING includes/db.php connection. Contains no
//  database username or password of its own.
// ============================================================

require_once __DIR__ . '/../includes/db.php';

header('Content-Type: application/json; charset=utf-8');

const SITE_URL = 'https://bethesdabiblicalinstitute.com/';

function fullUrl(?string $path): ?string {
    if (!$path) return null;
    if (str_starts_with($path, 'http://') || str_starts_with($path, 'https://')) return $path;
    return rtrim(SITE_URL, '/') . '/' . ltrim($path, '/');
}

try {
    // ── Home banner (falls back to defaults if no row / inactive) ──
    $banner = dbFetch("SELECT * FROM banners WHERE page_key='home' AND is_active=1");

    $bannerOut = [
        'title'      => trim($banner['title']    ?? '') ?: 'Welcome to Bethesda Biblical Institute',
        'subtitle'   => trim($banner['subtitle'] ?? '') ?: 'Equipping Ministers of the Gospel through Distance Theological Education',
        'button1_text' => trim($banner['btn1_text'] ?? '') ?: 'Apply Now',
        'button1_link' => trim($banner['btn1_link'] ?? '') ?: 'admissions.php',
        'button2_text' => trim($banner['btn2_text'] ?? '') ?: 'Learn More',
        'button2_link' => trim($banner['btn2_link'] ?? '') ?: 'about.php',
        'image'      => $banner && !empty($banner['image_path'])
            ? fullUrl('uploads/banners/' . $banner['image_path'])
            : null,
    ];

    // ── Testimonials ─────────────────────────────────────────────
    $testimonialRows = dbFetchAll(
        "SELECT name, role, quote, rating, photo FROM testimonials
         WHERE is_active = 1 ORDER BY sort_order ASC, id ASC LIMIT 10"
    );

    $testimonials = array_map(function ($row) {
        return [
            'name'   => $row['name'],
            'role'   => $row['role'],
            'quote'  => $row['quote'],
            'rating' => (int)$row['rating'],
            'photo'  => $row['photo'] ? fullUrl('uploads/testimonials/' . $row['photo']) : null,
        ];
    }, $testimonialRows);

    echo json_encode([
        'success'      => true,
        'banner'       => $bannerOut,
        'testimonials' => $testimonials,
    ]);

} catch (Throwable $e) {
    http_response_code(500);
    echo json_encode(['success' => false, 'error' => 'Server error']);
}

<?php
// ============================================================
//  api/news.php — JSON API for the Android app's News/Home tabs
//  Upload this file to: /bethesdabiblicalinstitute/api/news.php
//  (create an "api" folder alongside index.php if it doesn't exist)
//
//  This file reuses your EXISTING includes/db.php connection.
//  It does not — and should never — contain your DB username or
//  password itself. Those stay exactly where they already are,
//  on the server, in includes/db.php.
// ============================================================

require_once __DIR__ . '/../includes/db.php';

header('Content-Type: application/json; charset=utf-8');

$limit = isset($_GET['limit']) ? max(1, min(50, (int)$_GET['limit'])) : 10;
$slug  = trim($_GET['slug'] ?? '');

function newsSiteUrl(): string {
    return 'https://bethesdabiblicalinstitute.com/';
}

function resolveImage(array $row): ?string {
    $img = $row['image_url'] ?? $row['image_path'] ?? null;
    if (!$img) return null;
    // Turn a relative path stored in the DB into a full URL the app can load.
    if (str_starts_with($img, 'http://') || str_starts_with($img, 'https://')) {
        return $img;
    }
    return rtrim(newsSiteUrl(), '/') . '/' . ltrim($img, '/');
}

try {
    if ($slug !== '') {
        // Single post lookup (also bumps the view counter, same as news.php does)
        $post = dbFetch(
            "SELECT * FROM news WHERE slug=? AND status='published'",
            [$slug]
        );
        if (!$post) {
            http_response_code(404);
            echo json_encode(['success' => false, 'error' => 'Not found']);
            exit;
        }
        dbQuery("UPDATE news SET views = views + 1 WHERE id=?", [$post['id']]);

        echo json_encode([
            'success' => true,
            'post' => [
                'id'         => (int)$post['id'],
                'title'      => $post['title'],
                'slug'       => $post['slug'],
                'category'   => $post['category'],
                'excerpt'    => $post['excerpt'] ?? null,
                'content'    => $post['content'] ?? null,
                'image'      => resolveImage($post),
                'is_featured'=> (bool)$post['is_featured'],
                'event_date' => $post['event_date'] ?? null,
                'created_at' => $post['created_at'],
                'views'      => (int)$post['views'],
            ],
        ]);
        exit;
    }

    // List of published posts, most recent / featured first
    // Note: $limit is already validated as an int above (1-50), and PDO's
    // native prepared statements (emulate_prepares=false) can't bind LIMIT
    // as a parameter — so it's safely inlined directly here.
    $rows = dbFetchAll(
        "SELECT id, title, slug, category, excerpt, image_url, image_path,
                is_featured, event_date, created_at
         FROM news
         WHERE status = 'published'
         ORDER BY is_featured DESC, created_at DESC
         LIMIT {$limit}"
    );

    $items = array_map(function ($row) {
        return [
            'id'         => (int)$row['id'],
            'title'      => $row['title'],
            'slug'       => $row['slug'],
            'category'   => $row['category'],
            'excerpt'    => $row['excerpt'] ?? null,
            'image'      => resolveImage($row),
            'is_featured'=> (bool)$row['is_featured'],
            'event_date' => $row['event_date'] ?? null,
            'created_at' => $row['created_at'],
        ];
    }, $rows);

    echo json_encode(['success' => true, 'items' => $items]);

} catch (Throwable $e) {
    http_response_code(500);
    echo json_encode(['success' => false, 'error' => 'Server error']);
}

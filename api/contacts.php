<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json");

require 'db.php';

// Lire le JSON brut
$raw = file_get_contents("php://input");
file_put_contents("log.json", $raw); // debug optionnel

$data = json_decode($raw, true);

if (!isset($data['contacts']) || !is_array($data['contacts'])) {
    http_response_code(400);
    echo json_encode(["error" => "Invalid JSON"]);
    exit;
}

try {
    $stmt = $pdo->prepare("INSERT INTO contacts (name, number) VALUES (?, ?)");

    foreach ($data['contacts'] as $contact) {
        $stmt->execute([$contact['name'], $contact['number']]);
    }

    echo json_encode(["success" => true, "message" => "Contacts enregistrés"]);
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(["error" => $e->getMessage()]);
}

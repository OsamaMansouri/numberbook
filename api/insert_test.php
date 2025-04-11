<?php
// insert_test.php

require 'db.php';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $name = $_POST['name'] ?? '';
    $number = $_POST['number'] ?? '';

    if ($name && $number) {
        $stmt = $pdo->prepare("INSERT INTO contacts (name, number) VALUES (?, ?)");
        $stmt->execute([$name, $number]);
        $success = "Contact ajouté avec succès ✅";
    } else {
        $error = "Nom et numéro obligatoires ❌";
    }
}
?>

<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Test Insertion Contact</title>
</head>
<body>
    <h2>Ajouter un contact manuellement</h2>
    <?php if (!empty($success)) echo "<p style='color:green;'>$success</p>"; ?>
    <?php if (!empty($error)) echo "<p style='color:red;'>$error</p>"; ?>

    <form method="POST">
        <label>Nom :</label><br>
        <input type="text" name="name"><br><br>
        <label>Numéro :</label><br>
        <input type="text" name="number"><br><br>
        <button type="submit">Insérer</button>
    </form>
</body>
</html>

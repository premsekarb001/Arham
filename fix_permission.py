import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Remove the permissionLauncher and its LaunchedEffect
content = re.sub(
    r'val permissionLauncher = rememberLauncherForActivityResult.*?\}\s*\}\s*LaunchedEffect\(Unit\) \{\s*if \(android\.os\.Build\.VERSION\.SDK_INT >= android\.os\.Build\.VERSION_CODES\.TIRAMISU\) \{\s*permissionLauncher\.launch\(android\.Manifest\.permission\.POST_NOTIFICATIONS\)\s*\}\s*\}\s*',
    '',
    content,
    flags=re.DOTALL
)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

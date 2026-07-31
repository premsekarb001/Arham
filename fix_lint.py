import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Fix sharedPrefs.edit().putStringSet().apply()
content = content.replace(
    'sharedPrefs.edit().putStringSet("favorite_vacancies", newFavorites).apply()',
    'sharedPrefs.edit().putStringSet("favorite_vacancies", newFavorites).apply() // Replaced by core-ktx if needed, but standard is fine'
)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

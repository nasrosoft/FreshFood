import os
import shutil

base_dir = r'd:\github\FreshFood\AndroidApp'
old_pkg1 = 'com.freshfood.app'
old_pkg2 = 'com.example.freshfood'
new_pkg = 'com.devsoft.freshfood'

def replace_in_file(filepath):
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
    except UnicodeDecodeError:
        return # Skip binary files

    new_content = content.replace(old_pkg1, new_pkg).replace(old_pkg2, new_pkg)
    
    if new_content != content:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(new_content)
        print(f"Updated: {filepath}")

# 1. Update text inside files
for root, _, files in os.walk(base_dir):
    for file in files:
        if file.endswith(('.kt', '.xml', '.kts', '.pro', '.properties')):
            filepath = os.path.join(root, file)
            replace_in_file(filepath)

# 2. Rename the directories
java_dir = os.path.join(base_dir, r'app\src\main\java\com')
old_dir_app = os.path.join(java_dir, 'freshfood', 'app')
new_dir_devsoft = os.path.join(java_dir, 'devsoft')
new_dir_freshfood = os.path.join(new_dir_devsoft, 'freshfood')

# Create the target directory
os.makedirs(new_dir_freshfood, exist_ok=True)

# Move contents from com/freshfood/app to com/devsoft/freshfood
if os.path.exists(old_dir_app):
    for item in os.listdir(old_dir_app):
        s = os.path.join(old_dir_app, item)
        d = os.path.join(new_dir_freshfood, item)
        shutil.move(s, d)
    print("Moved directories to com/devsoft/freshfood")
    # Clean up old empty directories (com/freshfood/app and com/freshfood)
    try:
        os.rmdir(old_dir_app)
        os.rmdir(os.path.join(java_dir, 'freshfood'))
    except OSError:
        pass # Directory might not be empty if something else was there, but it should be.

print("Package refactoring completed.")

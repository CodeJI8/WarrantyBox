import os

base_path = r'c:\Users\noboj\AndroidStudioProjects\WarrantyBox\app\src\main\java\com\tools\warrantybox'
os.makedirs(os.path.join(base_path, 'ui'), exist_ok=True)
os.makedirs(os.path.join(base_path, 'ui', 'theme'), exist_ok=True)
os.makedirs(os.path.join(base_path, 'utils'), exist_ok=True)
print("Directories created")

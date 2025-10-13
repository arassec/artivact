import bpy
import os

# === CONFIGURATION ===
# Path to the project directory (absolute path is safest)
# If the script is run from within Blender and no file is open,
# it will use the current working directory.
project_dir = sys.argv[4]

# Default name for a new Blender project
blend_filename = "project.blend"

# === FUNCTIONS ===

def find_blend_file(directory):
    """Return the path of the first .blend file found in the directory, if any."""
    for file in os.listdir(directory):
        if file.endswith(".blend"):
            return os.path.join(directory, file)
    return None

def find_obj_file(directory):
    """Return the path of the first .obj file found in the directory, if any."""
    for file in os.listdir(directory):
        if file.endswith(".obj"):
            return os.path.join(directory, file)
    return None

# === MAIN LOGIC ===

existing_blend = find_blend_file(project_dir)

if existing_blend:
    print(f"Found existing Blender project: {existing_blend}")
    print("Opening existing project ...")
    bpy.ops.wm.open_mainfile(filepath=existing_blend)

else:
    print("No Blender project found. Creating a new one ...")

    # Start with an empty scene
    bpy.ops.wm.read_factory_settings(use_empty=True)

    obj_path = find_obj_file(project_dir)
    if obj_path:
        print(f"Importing OBJ file: {obj_path}")
        bpy.ops.import_scene.obj(filepath=obj_path)
        # center obj
        for obj in bpy.context.selected_objects:
            bpy.ops.object.origin_set(type='GEOMETRY_ORIGIN', center='MEDIAN')
    else:
        print("⚠️ No .obj file found in the directory!")

    # Save the new project
    new_blend_path = os.path.join(project_dir, blend_filename)
    bpy.ops.wm.save_as_mainfile(filepath=new_blend_path)
    print(f"New project saved at: {new_blend_path}")
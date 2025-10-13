import bpy
import os
import sys
from mathutils import *

MODEL_DIR = sys.argv[4]

# Default name for a new Blender project
blend_filename = "project.blend"

# === FUNCTIONS ===

def find_blend_file(directory):
    """Return the path of the first .blend file found in the directory, if any."""
    for file in os.listdir(directory):
        if file.endswith(".blend"):
            return os.path.join(directory, file)
    return None

# === MAIN LOGIC ===

# Disable splash screen...
bpy.context.preferences.view.show_splash = False
bpy.ops.wm.save_userpref()

existing_blend = find_blend_file(MODEL_DIR)

if existing_blend:
    print(f"Found existing Blender project: {existing_blend}")
    print("Opening existing project ...")
    bpy.ops.wm.open_mainfile(filepath=existing_blend)

else:
    file_list = os.listdir(MODEL_DIR)

    # prefer OBJ over GLB
    obj_list = [item for item in file_list if item.lower().endswith('.obj')]
    glb_list = [item for item in file_list if item.lower().endswith('.glb')]

    import_list = obj_list if obj_list else glb_list

    if not import_list:
        print("No OBJ or GLB files found.")
        sys.exit(0)

    for item in import_list:
        bpy.ops.object.select_all(action='SELECT')
        bpy.ops.object.delete(use_global=True)

        path_to_import = os.path.join(MODEL_DIR, item)

        # OBJ or GLB import
        if item.lower().endswith('.obj'):
            if bpy.app.version[0] < 4:
                bpy.ops.import_scene.obj(filepath=path_to_import)
            else:
                bpy.ops.wm.obj_import(filepath=path_to_import)
        else:
            # GLB/GLTF import
            bpy.ops.import_scene.gltf(filepath=path_to_import)

        # center objects
        for obj in bpy.context.selected_objects:
            bpy.ops.object.origin_set(type='GEOMETRY_ORIGIN', center='MEDIAN')

        path_to_export = os.path.join(MODEL_DIR, item.rsplit('.', 1)[0] + ".blend")
        bpy.ops.wm.save_as_mainfile(filepath=path_to_export, check_existing=False)

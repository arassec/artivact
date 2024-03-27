extends MeshInstance3D


# Called every frame. 'delta' is the elapsed time since the previous frame.
func _process(delta):
	rotate(Vector3(0, 1, 0), 0.4 * delta)

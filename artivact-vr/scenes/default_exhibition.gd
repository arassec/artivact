extends Node3D

var exhibitionId: String
var exhibitionZipFile: String
var exhibitionData: Variant

var zipReader: ZIPReader

var currentTopic


func setup(exhibitionIdInput: String):
	exhibitionId = exhibitionIdInput
	# TODO: Replace "res://" with "user://" once downloading exhibitions is implemented!
	exhibitionZipFile = str("res://", exhibitionId, ".artivact-exhibition.zip")
	
	zipReader = ZIPReader.new()
	var openResult := zipReader.open(exhibitionZipFile)
	if openResult != OK:
		# TODO: Error handling!
		return
	
	var exhibitionJson := JSON.new()
	var exhibitionJsonString = zipReader.read_file(str(exhibitionId, ".artivact.json")).get_string_from_utf8()
	var parseResult := exhibitionJson.parse(exhibitionJsonString)
	if parseResult != OK:
		# TODO: Error handling!
		return

	exhibitionData = exhibitionJson.data
	ArtivactSettings.setup(exhibitionData)


func _enter_tree():
	currentTopic = load("res://scenes/default_topic.tscn").instantiate()
	# TODO: Implement switching topics!
	currentTopic.setup(zipReader, exhibitionData.topics[0])
	add_child(currentTopic)


func _exit_tree():
	zipReader.close()


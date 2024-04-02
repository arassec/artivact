extends Node

# Contains the parsed exhibition JSON file, indexed by the exhibition's ID.
var exhibitions: Dictionary = {}

# Contains the ZIP-Readers of exhibitions, indexed by the exhibition's ID.
var exhibitionZipReaders: Dictionary = {}


func _init():
	load_exhibitions()


func load_exhibitions():
	var resourceFiles = DirAccess.get_files_at("res://")
	for resourceFile in resourceFiles:
		if resourceFile.ends_with(".artivact-exhibition.zip"):
			load_exhibition(resourceFile)


func load_exhibition(exhibitionFile: String):
	var exhibitionId = exhibitionFile.replace(".artivact-exhibition.zip", "")
	var exhibitionZipFile = str("res://", exhibitionFile)
	var exhibitionJsonFile = str(exhibitionId, ".artivact.json")
	
	var zipReader = ZIPReader.new()
	var openResult := zipReader.open(exhibitionZipFile)
	if openResult != OK:
		# TODO: Error handling!
		return
	
	var exhibitionJson := JSON.new()
	var exhibitionJsonString = zipReader.read_file(exhibitionJsonFile).get_string_from_utf8()
	var parseResult := exhibitionJson.parse(exhibitionJsonString)
	if parseResult != OK:
		# TODO: Error handling!
		return

	var exhibitionData = exhibitionJson.data
	
	exhibitions[exhibitionId] = exhibitionData
	exhibitionZipReaders[exhibitionId] = zipReader


func get_available_exhibitions() -> Dictionary:
	var result = {}
	for exhibitionId in exhibitions:
		result[exhibitionId] = I18n.translate(exhibitions[exhibitionId].title)
	return result


func get_zip_reader(exhibitionId: String):
	return exhibitionZipReaders[exhibitionId]


func get_exhibition(exhibitionId: String):
	return exhibitions[exhibitionId]


func get_exhibition_title(exhibitionId: String) -> String:
	return I18n.translate(exhibitions[exhibitionId].title)


func get_exhibition_description(exhibitionId: String) -> String:
	return I18n.translate(exhibitions[exhibitionId].description)


func get_property_categories(exhibitionId: String) -> Array[Variant]:
	return exhibitions[exhibitionId].propertyCategories


func get_tool(exhibitionId: String, topicIndex: int, toolIndex:) -> Variant:
	return exhibitions[exhibitionId].topics[topicIndex].tools[toolIndex]

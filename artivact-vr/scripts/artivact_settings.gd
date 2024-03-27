extends Node

var exhibitionData: Variant

var propertyCategories: Array[Variant]

func setup(exhibitionDataInput: Variant):
	exhibitionData = exhibitionDataInput
	propertyCategories = exhibitionData.propertyCategories


func get_exhibition_title() -> String:
	return exhibitionData.title.value


func get_exhibition_description() -> String:
	return exhibitionData.description.value


func get_property_categories() -> Array[Variant]:
	return propertyCategories


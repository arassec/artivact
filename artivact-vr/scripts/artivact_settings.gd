extends Node

var exhibitionData: Variant

var propertyCategories: Array[Variant]

func setup(exhibitionDataInput: Variant):
	exhibitionData = exhibitionDataInput
	propertyCategories = exhibitionData.propertyCategories


func get_property_categories() -> Array[Variant]:
	return propertyCategories

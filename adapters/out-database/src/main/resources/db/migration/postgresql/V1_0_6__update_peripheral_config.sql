UPDATE av_configuration SET content_json = REPLACE(content_json, 'FALLBACK_CAMERA_ADAPTER', 'DEFAULT_CAMERA_ADAPTER') WHERE id = 'ADAPTER';
UPDATE av_configuration SET content_json = REPLACE(content_json, 'DIGI_CAM_CONTROL_REMOTE_CAMERA_ADAPTER', 'DEFAULT_CAMERA_ADAPTER') WHERE id = 'ADAPTER';

UPDATE av_configuration SET content_json = REPLACE(content_json, 'FALLBACK_TURNTABLE_ADAPTER', 'DEFAULT_TURNTABLE_ADAPTER') WHERE id = 'ADAPTER';
UPDATE av_configuration SET content_json = REPLACE(content_json, 'ARTIVACT_TURNTABLE_ADAPTER', 'DEFAULT_TURNTABLE_ADAPTER') WHERE id = 'ADAPTER';

UPDATE av_configuration SET content_json = REPLACE(content_json, 'DEFAULT_BACKGROUND_REMOVAL_ADAPTER', 'DEFAULT_IMAGE_MANIPULATION_ADAPTER') WHERE id = 'ADAPTER';

UPDATE av_configuration SET content_json = REPLACE(content_json, 'REALITY_CAPTURE_MODEL_CREATOR_ADAPTER', 'REALITY_SCAN_MODEL_CREATOR_ADAPTER') WHERE id = 'ADAPTER';

UPDATE av_configuration SET content_json = REPLACE(content_json, 'ADAPTER', 'PERIPHERAL') WHERE id = 'ADAPTER';
UPDATE av_configuration SET content_json = REPLACE(content_json, 'Adapter', 'Peripheral') WHERE id = 'ADAPTER';
UPDATE av_configuration SET content_json = REPLACE(content_json, 'modelCreatorImplementation', 'modelCreatorPeripheralImplementation') WHERE id = 'ADAPTER';
UPDATE av_configuration SET content_json = REPLACE(content_json, 'modelEditorImplementation', 'modelEditorPeripheralImplementation') WHERE id = 'ADAPTER';

UPDATE av_configuration SET id = 'PERIPHERAL' WHERE id = 'ADAPTER';

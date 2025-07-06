<?xml version="1.0" encoding="UTF-8"?>
<batchjobs version="2.2.0">
  <job name="AlignPhotos" target="all">
    <mask_tiepoints>false</mask_tiepoints>
    <reference_preselection>false</reference_preselection>
  </job>
  <job name="BuildModel" target="all">
    <face_count_custom>100000</face_count_custom>
    <reuse_depth>true</reuse_depth>
  </job>
  <job name="BuildTexture" target="all">
    <texture_size>4096</texture_size>
  </job>
  <job name="ExportModel" target="all">
    <format>1</format>
    <path>##EXPORT_PATH##</path>
  </job>
</batchjobs>

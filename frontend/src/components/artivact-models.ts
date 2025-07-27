export interface Profiles {
  desktop: boolean;
  e2e: boolean;
}

export interface SelectboxModel {
  label: string;
  value: string;
  disable: boolean | undefined;
}

export interface TranslatableString {
  value: string;
  translatedValue: string;
  translations: Record<string, string>;
}

export interface BaseTranslatableRestrictedObject extends TranslatableString {
  id: string;
  restrictions: string[];
}

export interface BaseRestrictedObject {
  id: string;
  restrictions: string[];
}

export interface Asset {
  fileName: string;
  url: string;
  transferable: boolean;
}

export interface ItemCardData {
  itemId: string;
  title: TranslatableString;
  imageUrl: string;
  hasModel: boolean;
}

export interface ImageSet {
  modelInput: boolean;
  backgroundRemoved: boolean;
  images: Asset[];
}

export interface ModelSet {
  directory: string;
  comment: string;
  modelSetImage: string;
  modelFiles: Asset[];
}

export interface ItemDetails extends BaseRestrictedObject {
  version: number;
  title: TranslatableString;
  description: TranslatableString;
  images: Asset[];
  models: Asset[];
  creationImageSets: ImageSet[];
  creationModelSets: ModelSet[];
  properties: Record<string, TranslatableString>;
  tags: Tag[];
}

export interface UserData {
  authenticated: boolean;
  roles: string[];
  username: string;
}

export interface SearchResult {
  pageNumber: number;
  pageSize: number;
  totalPages: number;
  data: ItemCardData[];
}

export interface Property extends BaseTranslatableRestrictedObject {
  valueRange: BaseTranslatableRestrictedObject[];
}

export interface PropertyCategory extends BaseTranslatableRestrictedObject {
  properties: Property[];
}

export interface PropertiesConfiguration {
  categories: PropertyCategory[];
}

export interface Menu extends BaseTranslatableRestrictedObject {
  parentId: string | null;
  menuEntries: Menu[];
  targetPageId: string;
  targetPageAlias: string;
  hidden: boolean;
  external: string;
}

export interface Account {
  id: number | undefined;
  version: number;
  username: string;
  password: string;
  email: string;
  apiToken: string;
  user: boolean | undefined;
  admin: boolean | undefined;
}

export interface ExchangeConfiguration {
  remoteServer: string;
  apiToken: string;
}

export interface ColorTheme {
  primary: string,
  secondary: string,
  accent: string,
  dark: string,
  positive: string,
  negative: string,
  info: string,
  warning: string,
}

export interface License {
  prefix: TranslatableString;
  licenseLabel: TranslatableString;
  suffix: TranslatableString;
  licenseUrl: string;
}

export interface AppearanceConfiguration {
  applicationTitle: string,
  availableLocales: string,
  applicationLocale: string,
  colorTheme: ColorTheme,
  encodedFavicon: string,
  license: License
}

export interface ApplicationSettings {
  applicationTitle: string,
  availableLocales: string[],
  applicationLocale: string,
  colorTheme: ColorTheme,
  license: License,
  profiles: Profiles,
  availableRoles: string[],
  syncAvailable: boolean
}

export enum PeripheralImplementation {
  DEFAULT_IMAGE_MANIPULATION_PERIPHERAL = 'DEFAULT_IMAGE_MANIPULATION_PERIPHERAL',
  DEFAULT_CAMERA_PERIPHERAL='DEFAULT_CAMERA_PERIPHERAL',
  DIGI_CAM_CONTROL_CAMERA_PERIPHERAL='DIGI_CAM_CONTROL_CAMERA_PERIPHERAL',
  GPHOTO_TWO_CAMERA_PERIPHERAL='GPHOTO_TWO_CAMERA_PERIPHERAL',
  DEFAULT_TURNTABLE_PERIPHERAL='DEFAULT_TURNTABLE_PERIPHERAL',
  FALLBACK_MODEL_CREATOR_PERIPHERAL='FALLBACK_MODEL_CREATOR_PERIPHERAL',
  MESHROOM_MODEL_CREATOR_PERIPHERAL='MESHROOM_MODEL_CREATOR_PERIPHERAL',
  METASHAPE_MODEL_CREATOR_PERIPHERAL='METASHAPE_MODEL_CREATOR_PERIPHERAL',
  REALITY_SCAN_MODEL_CREATOR_PERIPHERAL = 'REALITY_SCAN_MODEL_CREATOR_PERIPHERAL',
  FALLBACK_MODEL_EDITOR_PERIPHERAL='FALLBACK_MODEL_EDITOR_PERIPHERAL',
  BLENDER_MODEL_EDITOR_PERIPHERAL='BLENDER_MODEL_EDITOR_PERIPHERAL'
}

export interface PeripheralConfiguration {
  imageManipulationPeripheralImplementation: PeripheralImplementation;
  availableImageManipulationPeripheralImplementations: PeripheralImplementation[];
  cameraPeripheralImplementation: PeripheralImplementation;
  availableCameraPeripheralImplementations: PeripheralImplementation[];
  turntablePeripheralImplementation: PeripheralImplementation;
  availableTurntablePeripheralImplementations: PeripheralImplementation[];
  modelCreatorPeripheralImplementation: PeripheralImplementation;
  availableModelCreatorPeripheralImplementations: PeripheralImplementation[];
  modelEditorPeripheralImplementation: PeripheralImplementation;
  availableModelEditorPeripheralImplementations: PeripheralImplementation[];
  configValues: Record<string, unknown>;
}

export interface Tag extends BaseTranslatableRestrictedObject {
  url: string;
  defaultTag: boolean;
}

export interface TagsConfiguration {
  tags: Tag[];
}

export interface Widget extends BaseRestrictedObject {
  type: string;
  navigationTitle: TranslatableString;
}

export interface PageContent extends BaseRestrictedObject {
  indexPage: boolean;
  widgets: Widget[];
  editable: boolean;
}

export interface BreadcrumbData {
  label: string;
  target: string | null;
  anchor: string | null;
}

export interface WidgetPageContainer {
  [key: string]: number
}

export interface CapturePhotosParams {
  numPhotos: number;
  useTurnTable: boolean;
  turnTableDelay: number;
  removeBackgrounds: boolean;
}

export interface OperationProgress {
  key: string;
  currentAmount: number;
  targetAmount: number;
  error: string;
}

export enum ContentSource {
  MENU='MENU',
  ITEM='ITEM',
}

export interface ExportConfiguration {
  applyRestrictions: boolean;
  optimizeSize: boolean;
  excludeItems: boolean;
}

export interface CollectionExport extends BaseRestrictedObject{
  title: TranslatableString;
  description: TranslatableString;
  content: TranslatableString;
  exportConfiguration: ExportConfiguration;
  contentSource: ContentSource;
  sourceId: string;
  fileLastModified: number;
  fileSize: number;
  filePresent: boolean;
  coverPictureExtension: string | null;
  distributionOnly: boolean;
}

export enum BatchProcessingTask {
  DELETE_ITEM='DELETE_ITEM',
  ADD_TAG_TO_ITEM='ADD_TAG_TO_ITEM',
  REMOVE_TAG_FROM_ITEM='REMOVE_TAG_FROM_ITEM',
  UPLOAD_MODIFIED_ITEM='UPLOAD_MODIFIED_ITEM'
}

export interface BatchProcessingParameters {
  task: BatchProcessingTask;
  searchTerm: string;
  maxItems: number;
  targetId: string;
}

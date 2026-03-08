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

export interface FavoriteItemData {
  itemId: string;
  title: string;
  thumbnailUrl: string;
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
  applicationTitle: string;
  availableLocales: string;
  applicationLocale: string;
  colorTheme: ColorTheme;
  encodedFavicon: string;
  license: License;
  indexPageId: string;
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
  ARDUINO_TURNTABLE_PERIPHERAL = 'ARDUINO_TURNTABLE_PERIPHERAL',
  PTP_CAMERA_PERIPHERAL = 'PTP_CAMERA_PERIPHERAL',
  EXTERNAL_PROGRAM_CAMERA_PERIPHERAL = 'EXTERNAL_PROGRAM_CAMERA_PERIPHERAL',
  ONNX_IMAGE_BACKGROUND_REMOVAL_PERIPHERAL = 'ONNX_IMAGE_BACKGROUND_REMOVAL_PERIPHERAL',
  EXTERNAL_PROGRAM_MODEL_CREATOR_PERIPHERAL = 'EXTERNAL_PROGRAM_MODEL_CREATOR_PERIPHERAL',
  EXTERNAL_PROGRAM_MODEL_EDITOR_PERIPHERAL = 'EXTERNAL_PROGRAM_MODEL_EDITOR_PERIPHERAL',
}

export interface PeripheralsConfiguration {
  turntablePeripheralConfigs: PeripheralConfig[];
  availableTurntablePeripheralImplementations: PeripheralImplementation[];
  cameraPeripheralConfigs: PeripheralConfig[];
  availableCameraPeripheralImplementations: PeripheralImplementation[];
  imageBackgroundRemovalPeripheralConfigs: PeripheralConfig[];
  availableImageBackgroundRemovalPeripheralImplementations: PeripheralImplementation[];
  modelCreatorPeripheralConfigs: PeripheralConfig[];
  availableModelCreatorPeripheralImplementations: PeripheralImplementation[];
  modelEditorPeripheralConfigs: PeripheralConfig[];
  availableModelEditorPeripheralImplementations: PeripheralImplementation[];
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

export interface PageMetaData {
  title: TranslatableString;
  description: TranslatableString;
  author: string;
  keywords: TranslatableString;
}

export interface PageContent extends BaseRestrictedObject {
  widgets: Widget[];
  editable: boolean;
  metaData: PageMetaData;
}

export interface BreadcrumbData {
  label: string;
  target: string | null;
  anchor: string | null;
}

export interface WidgetPageContainer {
  [key: string]: number
}

export interface CaptureImageParams {
  numPhotos: number;
  useTurnTable: boolean;
  removeBackgrounds: boolean;
  cameraPeripheralConfigId: string | null;
  turntablePeripheralConfigId: string | null;
  imageBackgroundRemovalPeripheralConfigId: string | null;
}

export interface CreateModelParams {
  modelCreatorPeripheralConfigId: string;
}

export interface OperationProgress {
  key: string;
  currentAmount: number;
  targetAmount: number;
  error: string;
}

export enum ContentSource {
  MENU = 'MENU',
  ITEM = 'ITEM',
}

export interface ExportConfiguration {
  applyRestrictions: boolean;
  xrExport: boolean;
  excludeItems: boolean;
}

export interface CollectionExport extends BaseRestrictedObject {
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
  DELETE_ITEM = 'DELETE_ITEM',
  ADD_TAG_TO_ITEM = 'ADD_TAG_TO_ITEM',
  REMOVE_TAG_FROM_ITEM = 'REMOVE_TAG_FROM_ITEM',
  UPLOAD_MODIFIED_ITEM = 'UPLOAD_MODIFIED_ITEM',
  UPDATE_SEARCH_INDEX = 'UPDATE_SEARCH_INDEX',
}

export interface BatchProcessingParameters {
  task: BatchProcessingTask;
  searchTerm: string;
  maxItems: number;
  targetId: string;
}

export interface ButtonConfig {
  targetUrl: string;
  iconLeft: string;
  label: TranslatableString;
  iconRight: string;
  size: number;
  buttonColor: string;
  textColor: string;
}

export interface PageIdAndAlias {
  id: string;
  alias: string;
}

export interface PeripheralConfig {
  id: string;
  peripheralImplementation: PeripheralImplementation;
  label: string;
  favourite: boolean;
}

export interface ArduinoTurntablePeripheralConfig extends PeripheralConfig {
  delayInMilliseconds: number;
}

export interface OnnxBackgroundRemovalPeripheralConfig
  extends PeripheralConfig {
  onnxModelFile: string;
  inputParameterName: string;
  imageWidth: number;
  imageHeight: number;
  numThreads: number;
}

export interface PtpCameraPeripheralConfig extends PeripheralConfig {
  delayInMilliseconds: number;
}

export interface ExternalProgramPeripheralConfig extends PeripheralConfig {
  command: string;
  arguments: string;
}

export interface ModelCreatorPeripheralConfig
  extends ExternalProgramPeripheralConfig {
  openInputDirInOs: boolean;
  resultDir: string;
}

export enum PeripheralStatus {
  AVAILABLE = 'AVAILABLE',
  ERROR = 'ERROR',
  DISCONNECTED = 'DISCONNECTED',
  NOT_EXECUTABLE = 'NOT_EXECUTABLE',
  FILE_DOESNT_EXIST = 'FILE_DOESNT_EXIST',
}

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
  exportTitle: TranslatableString;
  exportDescription: TranslatableString;
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
  availableRoles: string[]
}

export enum AdapterImplementation {
  FALLBACK_BACKGROUND_REMOVAL_ADAPTER='FALLBACK_BACKGROUND_REMOVAL_ADAPTER',
  REMBG_REMOTE_BACKGROUND_REMOVAL_ADAPTER='REMBG_REMOTE_BACKGROUND_REMOVAL_ADAPTER',
  FALLBACK_CAMERA_ADAPTER='FALLBACK_CAMERA_ADAPTER',
  DIGI_CAM_CONTROL_CAMERA_ADAPTER='DIGI_CAM_CONTROL_CAMERA_ADAPTER',
  DIGI_CAM_CONTROL_REMOTE_CAMERA_ADAPTER='DIGI_CAM_CONTROL_REMOTE_CAMERA_ADAPTER',
  GPHOTO_TWO_CAMERA_ADAPTER='GPHOTO_TWO_CAMERA_ADAPTER',
  FALLBACK_TURNTABLE_ADAPTER='FALLBACK_TURNTABLE_ADAPTER',
  ARTIVACT_TURNTABLE_ADAPTER='ARTIVACT_TURNTABLE_ADAPTER',
  FALLBACK_MODEL_CREATOR_ADAPTER='FALLBACK_MODEL_CREATOR_ADAPTER',
  MESHROOM_MODEL_CREATOR_ADAPTER='MESHROOM_MODEL_CREATOR_ADAPTER',
  METASHAPE_MODEL_CREATOR_ADAPTER='METASHAPE_MODEL_CREATOR_ADAPTER',
  REALITY_CAPTURE_MODEL_CREATOR_ADAPTER = 'REALITY_CAPTURE_MODEL_CREATOR_ADAPTER',
  FALLBACK_MODEL_EDITOR_ADAPTER='FALLBACK_MODEL_EDITOR_ADAPTER',
  BLENDER_MODEL_EDITOR_ADAPTER='BLENDER_MODEL_EDITOR_ADAPTER'
}

export interface AdapterConfiguration {
  backgroundRemovalAdapterImplementation: AdapterImplementation;
  availableBackgroundRemovalAdapterImplementations: AdapterImplementation[];
  cameraAdapterImplementation: AdapterImplementation;
  availableCameraAdapterImplementations: AdapterImplementation[];
  turntableAdapterImplementation: AdapterImplementation;
  availableTurntableAdapterImplementations: AdapterImplementation[];
  modelCreatorImplementation: AdapterImplementation;
  availableModelCreatorAdapterImplementations: AdapterImplementation[];
  modelEditorImplementation: AdapterImplementation;
  availableModelEditorAdapterImplementations: AdapterImplementation[];
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

export interface ExportConfiguration {
  applyRestrictions: boolean;
  optimizeSize: boolean;
}

export interface ContentExport {
  id: string;
  title: TranslatableString;
  description: TranslatableString;
  lastModified: string;
}

export enum BatchProcessingTask {
  DELETE_ITEM='DELETE_ITEM',
  ADD_TAG_TO_ITEM='ADD_TAG_TO_ITEM',
  REMOVE_TAG_FROM_ITEM='REMOVE_TAG_FROM_ITEM',
}

export interface BatchProcessingParameters {
  task: BatchProcessingTask;
  searchTerm: string;
  targetId: string;
}

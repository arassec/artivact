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

export interface BaseTranslatableRestrictedItem extends TranslatableString {
  id: string;
  restrictions: string[];
}

export interface BaseRestrictedItem {
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

export interface ItemDetails extends BaseRestrictedItem {
  version: number;
  title: TranslatableString;
  description: TranslatableString;
  images: Asset[];
  models: Asset[];
  creationImageSets: ImageSet[];
  creationModelSets: ModelSet[];
  properties: Record<string, string>;
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

export interface Property extends BaseTranslatableRestrictedItem {
  valueRange: BaseTranslatableRestrictedItem[];
}

export interface PropertyCategory extends BaseTranslatableRestrictedItem {
  properties: Property[];
}

export interface PropertiesConfiguration {
  categories: PropertyCategory[];
}

export interface Menu extends BaseTranslatableRestrictedItem {
  parentId: string | null;
  menuEntries: Menu[];
  targetPageId: string;
}

export interface MenuTreeNode extends Menu {
  disabled: boolean;
  selectable: boolean;
  expandable: boolean;
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

export interface LicenseConfiguration {
  prefix: TranslatableString;
  licenseLabel: TranslatableString;
  suffix: TranslatableString;
  licenseUrl: string;
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

export interface AppearanceConfiguration {
  applicationTitle: string,
  availableLocales: string,
  colorTheme: ColorTheme,
  encodedFaviconSmall: string,
  encodedFaviconLarge: string
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

export interface Tag extends BaseTranslatableRestrictedItem {
  url: string;
  defaultTag: boolean;
}

export interface TagsConfiguration {
  tags: Tag[];
}

export interface Widget extends BaseRestrictedItem {
  type: string;
}

export interface PageContent extends BaseRestrictedItem {
  indexPage: boolean;
  widgets: Widget[];
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
  progress: string;
  error: string;
}

export interface ExhibitionSummary {
  exhibitionId: string | null;
  title: TranslatableString;
  description: TranslatableString;
  menuIds: string[];
}

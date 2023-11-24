export interface SelectboxModel {
  label: string;
  value: string;
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

export interface MediaEntry {
  fileName: string;
  url: string;
}

export interface ItemCardData {
  itemId: string;
  title: TranslatableString;
  imageUrl: string;
  hasModel: boolean;
}

export interface ItemDetails {
  id: string;
  version: number;
  restrictions: string[];
  title: TranslatableString;
  description: TranslatableString;
  images: MediaEntry[];
  models: MediaEntry[];
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

export interface Account {
  id: number | undefined;
  version: number;
  username: string;
  password: string;
  email: string;
  user: boolean | undefined;
  admin: boolean | undefined;
}

export interface LicenseConfiguration {
  prefix: TranslatableString;
  licenseLabel: TranslatableString;
  suffix: TranslatableString;
  licenseUrl: string;
}

export interface ColorTheme {
  primary   : string,
  secondary : string,
  accent    : string,
  dark      : string,
  positive  : string,
  negative  : string,
  info      : string,
  warning   : string,
}

export interface AppearanceConfiguration {
  applicationTitle: string,
  availableLocales: string,
  colorTheme: ColorTheme,
  encodedFaviconSmall: string,
  encodedFaviconLarge: string
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

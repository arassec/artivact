export interface MediaEntry {
  fileName: string;
  url: string;
}

export interface ArtivactCardData {
  artivactId: string;
  title: string;
  imageUrl: string;
}

export interface ArtivactDetails {
  id: string;
  version: number;
  restrictions: string[];
  title: TranslatableItem;
  description: TranslatableItem;
  images: MediaEntry[];
  models: MediaEntry[];
  properties: Record<string, string>;
  tags: TranslatedTag[];
}

export interface TranslatableItem {
  id: string;
  value: string;
  restrictions: string[];
  translations: Record<string, string>;
}

export interface TranslatedItem extends TranslatableItem {
  translatedValue: string;
}

export interface Property extends TranslatableItem {
  valueRange: TranslatableItem[];
}

export interface PropertyCategory extends TranslatableItem {
  properties: Property[];
}

export interface PropertiesConfiguration {
  categories: PropertyCategory[];
}

export interface MenuEntry extends TranslatableItem {
  target: string;
}

export interface Menu extends TranslatableItem {
  menuEntries: MenuEntry[];
  target: string;
}

export interface MenuConfiguration {
  menus: Menu[];
}

export interface TranslatedProperty extends TranslatedItem {
  valueRange: TranslatedItem[];
  enteredValue: string;
}

export interface TranslatedPropertyCategory extends TranslatedItem {
  properties: TranslatedProperty[];
}

export interface TranslatedMenuEntry extends TranslatedItem {
  target: string;
}

export interface TranslatedMenu extends TranslatedItem {
  translatedMenuEntries: TranslatedMenuEntry[];
  target: string;
}

export interface SelectboxModel {
  label: string;
  value: string;
}

export interface UserData {
  authenticated: boolean;
  roles: string[];
  username: string;
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

export interface TranslatedLicense {
  prefix: string;
  licenseLabel: string;
  suffix: string;
  licenseUrl: string;
}

export interface LicenseConfiguration {
  prefix: TranslatableItem;
  licenseLabel: TranslatableItem;
  suffix: TranslatableItem;
  licenseUrl: string;
}

export interface Tag extends TranslatableItem {
  url: string;
}

export interface TagsConfiguration {
  tags: Tag[];
}

export interface TranslatedTag extends TranslatedItem {
  url: string;
}

export interface TranslatedTagsConfiguration {
  tags: TranslatedTag[];
}

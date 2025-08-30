import { ButtonConfig, TranslatableString, Widget } from '../artivact-models';

export interface SearchBasedWidgetData extends Widget {}

export interface PageTitleWidgetData extends Widget {
  title: TranslatableString;
  backgroundImage: string;
}

export interface TextWidgetData extends Widget {
  heading: TranslatableString;
  content: TranslatableString;
}

export interface InfoBoxWidgetData extends Widget {
  heading: TranslatableString;
  content: TranslatableString;
  boxType: string;
}

export interface ItemSearchWidget extends Widget {
  heading: TranslatableString;
  content: TranslatableString;
  searchTerm: string | null | undefined;
  maxResults: number;
  pageSize: number;
}

export interface AvatarWidgetData extends Widget {
  avatarImage: string;
  avatarSubtext: TranslatableString;
}

export enum ImageGalleryWidgetTextPosition {
  TOP = 'TOP',
  LEFT = 'LEFT',
  RIGHT = 'RIGHT',
}

export interface ImageGalleryWidgetData extends Widget {
  heading: TranslatableString;
  content: TranslatableString;
  images: string[];
  fullscreenAllowed: boolean;
  textPosition: ImageGalleryWidgetTextPosition;
  iconMode: boolean;
  hideBorder: boolean;
}

export interface ButtonsWidgetData extends Widget {
  columns: number;
  buttonConfigs: ButtonConfig[];
}

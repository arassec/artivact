import {TranslatableString, Widget} from 'components/models';

export interface PageTitleWidgetData extends Widget {
    title: TranslatableString;
    backgroundImage: string;
}

export interface TextWidgetData extends Widget {
    heading: TranslatableString;
    content: TranslatableString;
}

export interface SearchBasedWidgetData extends Widget {
    searchTerm: string | null | undefined;
    maxResults: number;
}

export interface InfoBoxWidgetData extends Widget {
    heading: TranslatableString;
    content: TranslatableString;
    boxType: string;
}

export interface AvatarWidgetData extends Widget {
  avatarImage: string;
  avatarSubtext: TranslatableString;
}

export interface SpaceWidgetData extends Widget {
  size: number;
}

export interface ImageTextWidgetData extends Widget {
  image: string;
  text: TranslatableString;
}

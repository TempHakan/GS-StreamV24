export interface Channel {
  id: string;
  name: string;
  streamUrl: string;
  logoUrl?: string;
  groupTitle?: string;
  tvgId?: string;
  tvgName?: string;
  streamType?: 'live' | 'vod' | 'series';
  resolution?: string;
  isFavorite?: boolean;
}

export interface Category {
  id: string;
  name: string;
  type: 'live' | 'vod' | 'series';
  channelCount?: number;
}

export interface EpgProgram {
  id: string;
  channelId: string;
  title: string;
  description?: string;
  start: string; // ISO String
  stop: string;  // ISO String
  progress?: number;
}

export interface VersionInfo {
  currentVersion: string;
  latestVersion: string;
  versionCode: number;
  downloadUrl: string;
  changelog: string[];
  mandatory: boolean;
  releaseDate: string;
}

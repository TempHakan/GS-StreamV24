import axios from 'axios';
import { Channel, Category } from '../models/types';

export class XtreamClient {
  private baseUrl: string;
  private user: string;
  private pass: string;

  constructor(host: string, user: string, pass: string) {
    this.baseUrl = host.endsWith('/') ? host.slice(0, -1) : host;
    this.user = encodeURIComponent(user);
    this.pass = encodeURIComponent(pass);
  }

  private getApiUrl(action?: string): string {
    let url = `${this.baseUrl}/player_api.php?username=${this.user}&password=${this.pass}`;
    if (action) url += `&action=${action}`;
    return url;
  }

  public async authenticate(): Promise<any> {
    const res = await axios.get(this.getApiUrl(), {
      timeout: 10000,
      headers: { 'User-Agent': 'IPTVSmarters/1.0 (StreamFlow IPTV)' }
    });
    return res.data;
  }

  public async getLiveCategories(): Promise<Category[]> {
    const res = await axios.get(this.getApiUrl('get_live_categories'), {
      timeout: 10000,
      headers: { 'User-Agent': 'IPTVSmarters/1.0' }
    });
    return (res.data || []).map((cat: any) => ({
      id: String(cat.category_id),
      name: cat.category_name,
      type: 'live'
    }));
  }

  public async getLiveStreams(categoryId?: string): Promise<Channel[]> {
    let url = this.getApiUrl('get_live_streams');
    if (categoryId) url += `&category_id=${categoryId}`;

    const res = await axios.get(url, {
      timeout: 15000,
      headers: { 'User-Agent': 'IPTVSmarters/1.0' }
    });

    return (res.data || []).map((ch: any) => ({
      id: String(ch.stream_id),
      name: ch.name,
      streamUrl: `${this.baseUrl}/live/${this.user}/${this.pass}/${ch.stream_id}.ts`,
      logoUrl: ch.stream_icon,
      groupTitle: ch.category_id,
      tvgId: ch.epg_channel_id,
      streamType: 'live'
    }));
  }

  public async getVodStreams(categoryId?: string): Promise<Channel[]> {
    let url = this.getApiUrl('get_vod_streams');
    if (categoryId) url += `&category_id=${categoryId}`;

    const res = await axios.get(url, {
      timeout: 15000,
      headers: { 'User-Agent': 'IPTVSmarters/1.0' }
    });

    return (res.data || []).map((item: any) => ({
      id: String(item.stream_id),
      name: item.name,
      streamUrl: `${this.baseUrl}/movie/${this.user}/${this.pass}/${item.stream_id}.${item.container_extension || 'mp4'}`,
      logoUrl: item.stream_icon,
      groupTitle: item.category_id,
      streamType: 'vod'
    }));
  }
}

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BaseService } from './base.service';
import { environment } from '../../../environments/environment';

const API_URL = `${environment.apiUrl}/api/categories`;

@Injectable({
  providedIn: 'root'
})
export class CategorieService extends BaseService<any> {

  constructor(http: HttpClient) {
    super(http, API_URL);
  }
}

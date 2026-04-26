import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BaseService } from './base.service';

const API_URL = 'http://localhost:8084/api/categories';

@Injectable({
  providedIn: 'root'
})
export class CategorieService extends BaseService<any> {

  constructor(http: HttpClient) {
    super(http, API_URL);
  }
}

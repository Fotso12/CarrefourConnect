import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

/**
 * Base abstract class for common API logic to reduce code duplication (DRY).
 * Provides standard CRUD operational wrappers and shared error handling.
 */
export abstract class BaseService<T> {
  
  constructor(
    protected readonly http: HttpClient,
    protected readonly baseUrl: string
  ) {}

  /**
   * Fetches all resources.
   */
  getAll(): Observable<T[]> {
    return this.http.get<T[]>(this.baseUrl).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Fetches a single resource by ID.
   */
  getById(id: string): Observable<T> {
    return this.http.get<T>(`${this.baseUrl}/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Creates a new resource.
   */
  create(item: T): Observable<T> {
    return this.http.post<T>(this.baseUrl, item).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Updates an existing resource.
   */
  update(id: string, item: T): Observable<T> {
    return this.http.put<T>(`${this.baseUrl}/${id}`, item).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Deletes a resource.
   */
  delete(id: string): Observable<any> {
    return this.http.delete<any>(`${this.baseUrl}/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Standard error handler for all HTTP requests.
   */
  protected handleError(error: HttpErrorResponse) {
    let errorMessage = 'Une erreur inconnue est survenue';
    if (error.error instanceof ErrorEvent) {
      errorMessage = `Erreur : ${error.error.message}`;
    } else {
      errorMessage = `Code d'erreur : ${error.status}\nMessage : ${error.message}`;
    }
    console.error(errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}

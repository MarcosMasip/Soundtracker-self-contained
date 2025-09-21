// movie.service.ts
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {MovieDto} from "../../models/movie/movie-dto.model";
import {Movie} from "../../models/movie/movie.model";


@Injectable({
  providedIn: 'root'
})
export class MovieService {
  // Use relative base URL so it works in both Docker (served via reverse proxy/service name) and local host without CORS issues.
  private baseUrl = '';

  constructor(private http: HttpClient) {
  }

  getAllMoviesDto(): Observable<MovieDto[]> {
    return this.http.get<MovieDto[]>(`${this.baseUrl}/api-soundtracker/db-movie/all-movies-dto`);
  }
  getMovieById(id: number): Observable<Movie> {
    return this.http.get<Movie>(`${this.baseUrl}/api-soundtracker/db-movie/info?id=${id}`);
  }
}

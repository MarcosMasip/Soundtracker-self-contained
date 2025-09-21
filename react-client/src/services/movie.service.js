import http from '../http-common';

class MovieService {
    getAllMoviesDto = () => {
        return http.get('/api-soundtracker/db-movie/all-movies-dto');
    };

    getMovieById = id => {
        return http.get(`/api-soundtracker/db-movie/info?id=${id}`);
    };
}
const movieService = new MovieService();
export default movieService;
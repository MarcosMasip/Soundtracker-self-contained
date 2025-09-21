import './App.css';
import React, {Component} from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';
import {Link, Route, Routes} from "react-router-dom";
import Home from "./components/home.component";
import MoviesList from "./components/movie-list.component";
import Movie from "./components/movie-details.component";

class App extends Component {
    render() {
        return (
            <div>
                <nav className="navbar navbar-expand navbar-dark bg-dark">
                    {/* Use Link so we stay inside /app/react without reloading root */}
                    <Link to="/" className="navbar-brand">
                        Soundtracker
                    </Link>
                    <div className="navbar-nav mr-auto">
                        <li className="nav-item">
                            <Link to={"/movies"} className="nav-link text-capitalize">
                                Movies
                            </Link>
                        </li>
                    </div>
                </nav>

                <div className="container mt-3">
                    <Routes>
                        <Route path="/" element={<Home/>}/>
                        <Route path="/movies" element={<MoviesList/>}/>
                        <Route path="/movies/:id" element={<Movie/>}/>
                    </Routes>
                </div>
            </div>
        );
    }
}

export default App;
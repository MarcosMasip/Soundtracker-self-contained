import React from 'react';
import './css/home.component.css';

const Home = () => {
  return (
    <div className="app-info">
      <img src="/clancy4.jpg" alt="App info" className="app-info-image" />
      <div className="app-info-text">
        <h1>Welcome to <strong>Soundtracker</strong></h1>
        <p>Soundtracker is an application that integrates with the Spotify and Kinopoisk APIs to retrieve rich data about
          movies and music.</p>
      </div>
    </div>
  );
};

export default Home;
import React from 'react';
import logo from './logo.svg';
import EventsView from './views/EventsView.jsx';
import CreateEventView from './views/CreateEventView.jsx';
import FeedbackView from './views/FeedbackView.jsx';
import HomeView from './views/HomeView.jsx';
import HostView from './views/HostView.jsx';
import RegisterView from './views/RegisterView.jsx';
import {
    BrowserRouter as Router,
    Switch,
    Route,
    Link
} from "react-router-dom";
import './App.css';
import 'bootstrap/dist/css/bootstrap.min.css';
import { Container, Jumbotron } from 'react-bootstrap';

// TODO Seems to be error with pages not changing when URL does until refreshed
function App() {
    return (
        <Jumbotron className="pt-4">
            <Router>
                <div>
                    <Switch>
                        <Route exact path="/">
                            <Link to="/event/new" className="btn btn-dark">+ New Event</Link>
                            <Link to="/events" className="btn btn-primary">My Events</Link>
                        </Route>

                        <Route path={["/event"]}>
                            <Link to="/" className="btn btn-primary">Back</Link>
                        </Route>
                    </Switch>
                    <Link to="/logout" className="btn btn-dark float-right">Logout</Link>
                </div>
            </Router>

            <Container className="border border bg-white rounded container-fluid  mt-2">
                <Router>
                    <Switch>
                        <Route exact path="/">
                            <HomeView />
                        </Route>
                        <Route exact path="/events">
                            <EventsView />
                        </Route>
                        <Route exact path="/event/new">
                            <CreateEventView />
                        </Route>
                        <Route exact path="/event/host">
                            <HostView />
                        </Route>
                        <Route exact path="/event/attendee">
                            <FeedbackView />
                        </Route>
                    </Switch>
                </Router>
            </Container>
        </Jumbotron>
    );
}

export default App;

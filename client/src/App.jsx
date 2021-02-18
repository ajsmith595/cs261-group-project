import React from 'react';
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
    Link,
    NavLink
} from "react-router-dom";
import 'bootstrap/dist/css/bootstrap.min.css';
import { Container, Jumbotron, Navbar } from 'react-bootstrap';

// TODO Seems to be error with pages not changing when URL does until refreshed
function App() {
    return (
        <Jumbotron className="pt-4">
            <Router>
                <div>
                    <Switch>
                        <Route exact path="/">
                            <Link to="/event/new" className="btn btn-dark m-1">+ New Event</Link>
                            <Link to="/events" className="btn btn-primary m-1">My Events</Link>
                        </Route>

                        <Route path={["/event"]}>
                            <Link to="/" className="btn btn-primary m-1">Back</Link>
                        </Route>
                    </Switch>
                    <Link to="/logout" className="btn btn-dark float-right m-1">Logout</Link>
                </div>

                <div className="border border bg-white rounded container-fluid  mt-2">
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
                </div>
            </Router>
        </Jumbotron >
    );
}

export default App;

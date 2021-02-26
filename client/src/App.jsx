import React from 'react';
import EventsView from './views/EventsView.jsx';
import CreateEventView from './views/CreateEventView.jsx';
import FeedbackView from './views/FeedbackView.jsx';
import HomeView from './views/HomeView.jsx';
import HostView from './views/HostView.jsx';
import RegisterView from './views/RegisterView.jsx';
import LoginView from './views/LoginView.jsx';
import LogoutView from './views/LogoutView.jsx';
import {
    BrowserRouter as Router,
    Switch,
    Route,
    Link,
    NavLink
} from "react-router-dom";
import 'bootstrap/dist/css/bootstrap.min.css';
import { Container, Jumbotron, Navbar } from 'react-bootstrap';
import AttendeeHostView from './views/AttendeeHostView.jsx';

// TODO Seems to be error with pages not changing when URL does until refreshed
function App() {
    return (

        <Jumbotron className="pt-4">
            <Router>
                <div>
                    <Switch>
                        <Route exact path="/">
                            <Link key="new_event_btn" to="/event/new" className="btn btn-dark m-1">+ New Event</Link>
                            <Link key="my_events_btn" to="/events" className="btn btn-primary m-1">My Events</Link>
                        </Route>

                        <Route path={["/event"]}>
                            <Link key="back_btn" to="/" className="btn btn-primary m-1">Back</Link>
                        </Route>
                    </Switch>
                    <Switch>
                        <Route path={["/login", "/register"]} />
                        <Route path="/">
                            <Link key="logout_btn" to="/logout" className="btn btn-dark float-right m-1">Logout</Link>
                        </Route>
                    </Switch>
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
                        <Route path="/event/:id" component={AttendeeHostView} />
                        <Route path="/register">
                            <RegisterView />
                        </Route>
                        <Route path="/login">
                            <LoginView />
                        </Route>
                        <Route path="/logout">
                            <LogoutView />
                        </Route>
                    </Switch>
                </div>
            </Router>
        </Jumbotron >
    );
}

export default App;

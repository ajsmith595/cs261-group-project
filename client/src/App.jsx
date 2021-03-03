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
import { Jumbotron } from 'react-bootstrap';
import AttendeeHostView from './views/AttendeeHostView.jsx';
import { CSSTransition, TransitionGroup } from 'react-transition-group';
import "./App.css";

function App() {
    return (
        <Jumbotron className="py-2 mb-0 min-vh-100">
            <Router>
                <div className="clearfix">
                    <Switch>
                        <Route exact path="/">
                            <Link key="new_event_btn" to="/event/new" className="btn btn-dark m-1">+ New Event</Link>
                            <Link key="my_events_btn" to="/events" className="btn btn-primary m-1">My Events</Link>
                        </Route>

                        <Route path={["/event", "/events"]}>
                            <Link key="back_btn" to="/" className="btn btn-primary m-1">Back</Link>
                        </Route>
                    </Switch>
                    <Switch>
                        <Route path="/login">
                            <Link key="register_btn" to="/register" className="btn btn-primary m-1 float-right">Register</Link>
                        </Route>

                        <Route path="/register">
                            <Link key="login_btn" to="/login" className="btn btn-primary m-1 float-right">Login</Link>
                        </Route>
                        <Route path="/">
                            <Link key="logout_btn" to="/logout" className="btn btn-dark float-right m-1">Logout</Link>
                        </Route>
                    </Switch>
                </div>


                <Route render={({ location }) => (
                    <TransitionGroup className="main-view">
                        <CSSTransition
                            timeout={300}
                            classNames="fade-animation"
                            key={location.key}
                        >
                            <div className="border border bg-white rounded container-fluid mt-2">
                                <Switch location={location}>
                                    <Route exact path="/" component={HomeView} />
                                    <Route exact path="/events" component={EventsView} />
                                    <Route exact path="/event/new" component={CreateEventView} />
                                    <Route path="/event/:id" component={AttendeeHostView} />
                                    <Route path="/register" component={RegisterView} />
                                    <Route path="/login" component={LoginView} />
                                    <Route path="/logout" component={LogoutView} />
                                </Switch>
                            </div>
                        </CSSTransition>
                    </TransitionGroup>
                )} />
            </Router>
        </Jumbotron >
    );
}

export default App;

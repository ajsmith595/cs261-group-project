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

function App() {
    return (
        <Jumbotron className="pt-4">
            <Router>
                <div>
                    <Route path="/event/">
                        <Link to="/" className="btn btn-primary">Back</Link>
                        <Link to="/logout" className="btn btn-dark float-right">Logout</Link>
                    </Route>
                </div>
                <Container className="border border bg-white rounded container-fluid  mt-2">
                    <Switch>
                        <Route path="/events">
                            <EventsView />
                        </Route>
                        <Route path="/event/host">
                            <HostView />
                        </Route>
                        <Route path="/event/attendee">
                            <FeedbackView />
                        </Route>
                    </Switch>
                </Container>
            </Router>
        </Jumbotron>
    );
}

export default App;

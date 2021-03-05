import React from 'react';
import EventsView from './views/EventsView.jsx';
import CreateEventView from './views/CreateEventView.jsx';
import FeedbackView from './views/FeedbackView.jsx';
import HomeView from './views/HomeView.jsx';
import HostView from './views/HostView.jsx';
import RegisterView from './views/RegisterView.jsx';
import LoginView from './views/LoginView.jsx';
import LogoutView from './views/LogoutView.jsx';
import EditEventView from './views/EditEventView.jsx';
import { parse as ParseQueryString } from 'query-string';
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
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faArrowLeft, faEdit, faSignInAlt, faSignOutAlt, faUser, faPlus, faList } from '@fortawesome/free-solid-svg-icons';

class App extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            isHost: false
        }
    }
    render() {
        return (
            <Jumbotron className="py-2 mb-0 min-vh-100" style={{ overflowX: 'hidden' }}>
                <Router>
                    <div className="clearfix">
                        <Switch>
                            <Route exact path="/">
                                <Link key="new_event_btn" to="/event/new" className="btn btn-success m-1"><FontAwesomeIcon icon={faPlus} /> New Event</Link>
                                <Link key="my_events_btn" to="/events" className="btn btn-secondary m-1"><FontAwesomeIcon icon={faList} /> My Events</Link>
                            </Route>
                            <Route exact path="/event/:id/edit" render={(props) => {
                                let url = `/event/${props.match.params.id}`;
                                let queryParams = ParseQueryString(props.location.search);
                                if (queryParams.back) {
                                    url += "?back=" + queryParams.back;
                                }
                                return <Link key="edit_back_btn" to={url} className="btn btn-secondary m-1"><FontAwesomeIcon icon={faArrowLeft} /> Back</Link>
                            }} />
                            <Route path={["/event", "/events"]} render={(props) => {
                                let url = "/";
                                let queryParams = ParseQueryString(props.location.search);
                                if (queryParams.back) {
                                    url = "/events";
                                }
                                return <Link key="back_btn" to={url} className="btn btn-secondary m-1"><FontAwesomeIcon icon={faArrowLeft} /> Back</Link>;
                            }}>

                            </Route>
                        </Switch>
                        <Switch>
                            <Route path="/login">
                                <Link key="register_btn" to="/register" className="btn btn-primary m-1 float-right"><FontAwesomeIcon icon={faUser} /> Register</Link>
                            </Route>

                            <Route path="/register">
                                <Link key="login_btn" to="/login" className="btn btn-primary m-1 float-right"><FontAwesomeIcon icon={faSignInAlt} /> Login</Link>
                            </Route>
                            <Route path="/">
                                <Link key="logout_btn" to="/logout" className="btn btn-outline-danger font-weight-bold float-right m-1"><FontAwesomeIcon icon={faSignOutAlt} /> Logout</Link>
                            </Route>

                        </Switch>
                        <Switch>
                            <Route exact path="/event/new" />
                            <Route exact path="/event/:id" render={(props) => {
                                if (!this.state.isHost) return null;
                                let url = `/event/${props.match.params.id}/edit`;
                                let queryParams = ParseQueryString(props.location.search);
                                if (queryParams.back) {
                                    url += "?back=" + queryParams.back;
                                }
                                return <Link key="edit_event_btn" to={url} className="btn btn-primary m-1"><FontAwesomeIcon icon={faEdit} /> Edit Event</Link>
                            }} />
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
                                        <Route path="/event/:id/edit" component={EditEventView} />
                                        <Route path="/event/:id" render={(props) => {
                                            return <AttendeeHostView {...props} isHostCallback={(isHost) => this.setState({ isHost: isHost })} />
                                        }} />
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
}

export default App;

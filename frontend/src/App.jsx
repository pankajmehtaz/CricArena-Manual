import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import AppShell from './components/layout/AppShell';
import { AuthProvider } from './context/AuthContext';
import { CollegeProvider } from './context/CollegeContext';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import MatchesPage from './pages/MatchesPage';
import MatchDetailPage from './pages/MatchDetailPage';
import MatchSetupPage from './pages/MatchSetupPage';
import LiveScoringPage from './pages/LiveScoringPage';
import TournamentDetailPage from './pages/TournamentDetailPage';
import TournamentsPage from './pages/TournamentsPage';
import TeamDetailPage from './pages/TeamDetailPage';
import TeamsPage from './pages/TeamsPage';
import AcceptInvitationPage from './pages/AcceptInvitationPage';

export default function App() {
  return (
    <AuthProvider>
      <CollegeProvider>
        <BrowserRouter>
          <Routes>
            <Route element={<AppShell />}>
              <Route path="/" element={<HomePage />} />
              <Route path="/matches" element={<MatchesPage />} />
              <Route path="/cricket" element={<Navigate to="/matches" replace />} />
              <Route path="/tournaments" element={<TournamentsPage />} />
              <Route path="/tournaments/:tournamentId" element={<TournamentDetailPage />} />
              <Route path="/teams" element={<TeamsPage />} />
              <Route path="/teams/:teamId" element={<TeamDetailPage />} />
              <Route path="/matches/:matchId" element={<MatchDetailPage />} />
              <Route path="/matches/:matchId/scorecard" element={<MatchDetailPage />} />
              <Route path="/matches/:matchId/setup" element={<MatchSetupPage />} />
              <Route path="/matches/:matchId/score" element={<LiveScoringPage />} />
              <Route path="/login" element={<LoginPage />} />
              <Route path="/accept-invitation" element={<AcceptInvitationPage />} />
              <Route path="*" element={<Navigate to="/" replace />} />
            </Route>
          </Routes>
        </BrowserRouter>
      </CollegeProvider>
    </AuthProvider>
  );
}

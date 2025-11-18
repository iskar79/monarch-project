import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

const PrivateRoute = () => {
  const { isAuthenticated } = useAuth();

  // 인증 상태를 확인하는 동안 로딩 상태를 표시합니다.
  if (isAuthenticated === null) {
    return <div>Loading...</div>; // 또는 스피너 컴포넌트를 사용할 수 있습니다.
  }

  // 인증되었으면 요청된 페이지를 보여주고, 그렇지 않으면 로그인 페이지로 이동
  return isAuthenticated ? <Outlet /> : <Navigate to="/login" replace />;
};

export default PrivateRoute;
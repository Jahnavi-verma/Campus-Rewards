import { useHealthCheck, getHealthCheckQueryKey, useGetRecyclingStats, getGetRecyclingStatsQueryKey, useGetRecentSubmissions, getGetRecentSubmissionsQueryKey, useGetLeaderboard, getGetLeaderboardQueryKey } from "@workspace/api-client-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Activity, Recycle, Users, Trophy, Box, Leaf } from "lucide-react";
import { Skeleton } from "@/components/ui/skeleton";
import { LoginRequired } from "@/components/login-required";

export default function DashboardPage() {
  const { data: health, isLoading: healthLoading } = useHealthCheck({
    query: { refetchInterval: 30000, queryKey: getHealthCheckQueryKey() }
  });

  const { data: stats, isLoading: statsLoading, error: statsError } = useGetRecyclingStats({
    query: { refetchInterval: 30000, queryKey: getGetRecyclingStatsQueryKey(), retry: false }
  });

  const { data: recent, isLoading: recentLoading } = useGetRecentSubmissions({
    query: { refetchInterval: 30000, queryKey: getGetRecentSubmissionsQueryKey(), retry: false }
  });

  const { data: leaderboard, isLoading: leaderboardLoading } = useGetLeaderboard({
    query: { refetchInterval: 30000, queryKey: getGetLeaderboardQueryKey(), retry: false }
  });

  const isAuthError = statsError?.status === 401 || statsError?.status === 403;

  return (
    <>
      <div className="flex flex-col md:flex-row md:items-end justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Overview</h1>
          <p className="text-muted-foreground mt-1">Real-time campus recycling metrics.</p>
        </div>
        
        <Card className="w-fit bg-secondary/50 border-secondary">
          <CardContent className="p-3 py-2 flex items-center gap-3">
            <div className={`w-2 h-2 rounded-full ${health?.status === 'UP' ? 'bg-green-500 animate-pulse' : 'bg-red-500'}`} />
            <div className="text-sm font-medium">
              Backend Status: {healthLoading ? 'Checking...' : health?.status || 'DOWN'}
            </div>
          </CardContent>
        </Card>
      </div>

      {isAuthError ? (
        <LoginRequired 
          title="Admin Access Required" 
          description="The dashboard overview requires administrator privileges to view campus-wide statistics."
        />
      ) : (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <StatCard 
              title="Total Submissions" 
              value={stats?.totalSubmissions} 
              loading={statsLoading} 
              icon={Activity} 
            />
            <StatCard 
              title="Total Points" 
              value={stats?.totalPoints} 
              loading={statsLoading} 
              icon={Leaf} 
            />
            <StatCard 
              title="Bottles & Cans" 
              value={(stats?.totalBottles || 0) + (stats?.totalCans || 0)} 
              loading={statsLoading} 
              icon={Recycle} 
            />
            <StatCard 
              title="Active Students" 
              value={stats?.totalUsers} 
              loading={statsLoading} 
              icon={Users} 
            />
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <Card className="lg:col-span-2">
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Box className="w-5 h-5 text-primary" />
                  Recent Activity
                </CardTitle>
              </CardHeader>
              <CardContent>
                {recentLoading ? (
                  <div className="space-y-4">
                    {[1, 2, 3].map(i => <Skeleton key={i} className="h-12 w-full" />)}
                  </div>
                ) : recent && recent.length > 0 ? (
                  <div className="space-y-4">
                    {recent.slice(0, 5).map((sub) => (
                      <div key={sub.id} className="flex items-center justify-between border-b pb-4 last:border-0 last:pb-0">
                        <div>
                          <p className="font-medium">{sub.userName || 'Anonymous'}</p>
                          <p className="text-sm text-muted-foreground flex gap-2">
                            <span>{sub.quantity}x {sub.itemType}</span>
                            <span>&bull;</span>
                            <span>{sub.location || 'Unknown location'}</span>
                          </p>
                        </div>
                        <div className="text-right">
                          <p className="font-bold text-primary">+{sub.pointsEarned} CP</p>
                          <p className="text-xs text-muted-foreground">
                            {sub.submittedAt ? new Date(sub.submittedAt).toLocaleTimeString() : ''}
                          </p>
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="text-center py-8 text-muted-foreground">No recent submissions found.</div>
                )}
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Trophy className="w-5 h-5 text-amber-500" />
                  Top Students
                </CardTitle>
              </CardHeader>
              <CardContent>
                {leaderboardLoading ? (
                  <div className="space-y-4">
                    {[1, 2, 3].map(i => <Skeleton key={i} className="h-10 w-full" />)}
                  </div>
                ) : leaderboard && leaderboard.length > 0 ? (
                  <div className="space-y-4">
                    {leaderboard.slice(0, 5).map((user, i) => (
                      <div key={user.id} className="flex items-center gap-3">
                        <div className={`w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold ${i === 0 ? 'bg-amber-100 text-amber-700' : i === 1 ? 'bg-gray-100 text-gray-700' : i === 2 ? 'bg-orange-100 text-orange-800' : 'bg-secondary text-secondary-foreground'}`}>
                          {i + 1}
                        </div>
                        <div className="flex-1 min-w-0">
                          <p className="text-sm font-medium truncate">{user.name}</p>
                          <p className="text-xs text-muted-foreground truncate">{user.levelTitle}</p>
                        </div>
                        <div className="font-mono text-sm font-bold">
                          {user.points}
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="text-center py-8 text-muted-foreground">No leaderboard data.</div>
                )}
              </CardContent>
            </Card>
          </div>
        </>
      )}
    </>
  );
}

function StatCard({ title, value, loading, icon: Icon }: { title: string, value?: number, loading: boolean, icon: any }) {
  return (
    <Card>
      <CardContent className="p-6">
        <div className="flex items-center justify-between space-y-0 pb-2">
          <p className="tracking-tight text-sm font-medium text-muted-foreground">{title}</p>
          <Icon className="h-4 w-4 text-muted-foreground" />
        </div>
        <div className="text-3xl font-bold font-mono">
          {loading ? <Skeleton className="h-9 w-24" /> : (value?.toLocaleString() || 0)}
        </div>
      </CardContent>
    </Card>
  );
}

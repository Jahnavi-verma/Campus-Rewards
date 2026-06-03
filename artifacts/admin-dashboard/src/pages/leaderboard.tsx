import { useGetLeaderboard, getGetLeaderboardQueryKey } from "@workspace/api-client-react";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Trophy } from "lucide-react";
import { Skeleton } from "@/components/ui/skeleton";
import { Progress } from "@/components/ui/progress";
import { LoginRequired } from "@/components/login-required";
import { Badge } from "@/components/ui/badge";

export default function LeaderboardPage() {
  const { data: leaderboard, isLoading, error } = useGetLeaderboard({
    query: { refetchInterval: 30000, queryKey: getGetLeaderboardQueryKey(), retry: false }
  });

  const isAuthError = error?.status === 401 || error?.status === 403;

  if (isAuthError) {
    return <LoginRequired title="Admin Access Required" description="You must be logged in to view the student leaderboard." />;
  }

  return (
    <>
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Leaderboard</h1>
        <p className="text-muted-foreground mt-1">Top 20 students by Campus Points.</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Trophy className="w-5 h-5 text-primary" />
            Campus Rankings
          </CardTitle>
          <CardDescription>
            Levels: L1 Sapling (0-49) &rarr; L2 Sprout (50-149) &rarr; L3 Plant (150-349) &rarr; L4 Tree (350-699) &rarr; L5 Legendary Tree (700+)
          </CardDescription>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="space-y-4">
              {[...Array(5)].map((_, i) => (
                <Skeleton key={i} className="h-16 w-full" />
              ))}
            </div>
          ) : leaderboard && leaderboard.length > 0 ? (
            <div className="rounded-md border">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead className="w-16 text-center">Rank</TableHead>
                    <TableHead>Student</TableHead>
                    <TableHead>Level Title</TableHead>
                    <TableHead className="w-1/3">Progress to Next Level</TableHead>
                    <TableHead className="text-right">Points</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {leaderboard.map((user, index) => (
                    <TableRow key={user.id}>
                      <TableCell className="text-center font-bold">
                        {index === 0 ? <span className="text-amber-500 text-lg">1</span> : 
                         index === 1 ? <span className="text-gray-400 text-lg">2</span> : 
                         index === 2 ? <span className="text-orange-600 text-lg">3</span> : 
                         <span className="text-muted-foreground">{index + 1}</span>}
                      </TableCell>
                      <TableCell className="font-medium">
                        <div className="flex items-center gap-2">
                          {user.name}
                          {index === 0 && <Badge variant="secondary" className="text-xs bg-amber-100 text-amber-800 hover:bg-amber-100 border-amber-200">Top Recycler</Badge>}
                        </div>
                      </TableCell>
                      <TableCell>
                        <Badge variant="outline" className="bg-primary/5 text-primary border-primary/20">
                          {user.levelTitle} (L{user.level})
                        </Badge>
                      </TableCell>
                      <TableCell>
                        {user.level < 5 ? (
                          <div className="flex items-center gap-3">
                            <Progress value={user.levelProgressPercent || 0} className="h-2 flex-1" />
                            <span className="text-xs text-muted-foreground w-12 text-right">
                              {Math.round(user.levelProgressPercent || 0)}%
                            </span>
                          </div>
                        ) : (
                          <span className="text-xs font-medium text-primary uppercase tracking-wider">MAX LEVEL</span>
                        )}
                      </TableCell>
                      <TableCell className="text-right font-mono font-bold text-lg">
                        {user.points.toLocaleString()}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          ) : (
            <div className="text-center py-12 text-muted-foreground">
              No students found on the leaderboard.
            </div>
          )}
        </CardContent>
      </Card>
    </>
  );
}

import { useGetRecentSubmissions, getGetRecentSubmissionsQueryKey } from "@workspace/api-client-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Activity } from "lucide-react";
import { Skeleton } from "@/components/ui/skeleton";
import { LoginRequired } from "@/components/login-required";
import { Badge } from "@/components/ui/badge";

export default function SubmissionsPage() {
  const { data: submissions, isLoading, error } = useGetRecentSubmissions({
    query: { refetchInterval: 30000, queryKey: getGetRecentSubmissionsQueryKey(), retry: false }
  });

  const isAuthError = error?.status === 401 || error?.status === 403;

  if (isAuthError) {
    return <LoginRequired title="Admin Access Required" description="You must be logged in to view recent submissions data." />;
  }

  return (
    <>
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Recent Submissions</h1>
        <p className="text-muted-foreground mt-1">Live feed of recycling activity across campus.</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Activity className="w-5 h-5 text-primary" />
            Activity Feed
          </CardTitle>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="space-y-4">
              {[...Array(8)].map((_, i) => (
                <Skeleton key={i} className="h-12 w-full" />
              ))}
            </div>
          ) : submissions && submissions.length > 0 ? (
            <div className="rounded-md border">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Time</TableHead>
                    <TableHead>Student</TableHead>
                    <TableHead>Item</TableHead>
                    <TableHead className="text-right">Qty</TableHead>
                    <TableHead>Location</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead className="text-right">Points Earned</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {submissions.map((sub) => (
                    <TableRow key={sub.id}>
                      <TableCell className="text-muted-foreground text-sm whitespace-nowrap">
                        {sub.submittedAt ? new Date(sub.submittedAt).toLocaleString() : 'Unknown'}
                      </TableCell>
                      <TableCell className="font-medium">
                        {sub.userName || 'Anonymous'}
                      </TableCell>
                      <TableCell>
                        <Badge variant="outline" className="bg-secondary/50">
                          {sub.itemType}
                        </Badge>
                      </TableCell>
                      <TableCell className="text-right font-mono">
                        {sub.quantity}
                      </TableCell>
                      <TableCell className="text-sm">
                        {sub.location || <span className="text-muted-foreground italic">Not specified</span>}
                      </TableCell>
                      <TableCell>
                        <Badge 
                          variant={sub.status === 'APPROVED' ? 'default' : 'secondary'}
                          className={sub.status === 'APPROVED' ? 'bg-green-100 text-green-800 hover:bg-green-100 border-transparent' : ''}
                        >
                          {sub.status}
                        </Badge>
                      </TableCell>
                      <TableCell className="text-right font-bold text-primary">
                        +{sub.pointsEarned} CP
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          ) : (
            <div className="text-center py-12 text-muted-foreground">
              No recent submissions found.
            </div>
          )}
        </CardContent>
      </Card>
    </>
  );
}

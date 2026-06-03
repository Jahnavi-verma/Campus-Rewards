import { useState, useEffect } from "react";
import { useHealthCheck, getHealthCheckQueryKey, useGetRecyclingItems, getGetRecyclingItemsQueryKey } from "@workspace/api-client-react";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Server, Settings, CheckCircle2, XCircle } from "lucide-react";
import { Skeleton } from "@/components/ui/skeleton";

export default function ApiStatusPage() {
  const [lastChecked, setLastChecked] = useState<Date>(new Date());
  
  const { data: health, isLoading: healthLoading, isError: healthError } = useHealthCheck({
    query: { refetchInterval: 30000, queryKey: getHealthCheckQueryKey() }
  });

  const { data: itemsInfo, isLoading: itemsLoading } = useGetRecyclingItems({
    query: { refetchInterval: 30000, queryKey: getGetRecyclingItemsQueryKey() }
  });

  // Update last checked time whenever data is fetched
  useEffect(() => {
    if (health || itemsInfo) {
      setLastChecked(new Date());
    }
  }, [health, itemsInfo]);

  return (
    <>
      <div className="flex flex-col md:flex-row md:items-end justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">API Monitor</h1>
          <p className="text-muted-foreground mt-1">Live health checks and system configuration.</p>
        </div>
        <div className="text-sm text-muted-foreground">
          Last checked: <span className="font-mono">{lastChecked.toLocaleTimeString()}</span>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Server className="w-5 h-5 text-primary" />
              Health Status
            </CardTitle>
            <CardDescription>
              /api/healthz endpoint status
            </CardDescription>
          </CardHeader>
          <CardContent>
            {healthLoading ? (
              <Skeleton className="h-24 w-full" />
            ) : healthError ? (
              <div className="flex flex-col items-center justify-center p-6 bg-destructive/10 rounded-lg border border-destructive/20 text-destructive">
                <XCircle className="w-10 h-10 mb-2" />
                <h3 className="font-bold text-lg">System Offline</h3>
                <p className="text-sm">Cannot reach backend API.</p>
              </div>
            ) : (
              <div className="flex flex-col items-center justify-center p-6 bg-green-50 dark:bg-green-950/20 rounded-lg border border-green-200 dark:border-green-900 text-green-700 dark:text-green-400">
                <CheckCircle2 className="w-10 h-10 mb-2" />
                <h3 className="font-bold text-lg">System Online</h3>
                <p className="text-sm font-mono mt-1 bg-white/50 dark:bg-black/20 px-2 py-1 rounded">
                  Status: {health?.status} | Service: {health?.service || 'API'}
                </p>
              </div>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Settings className="w-5 h-5 text-primary" />
              Recycling Configuration
            </CardTitle>
            <CardDescription>
              /api/recycling/items endpoint configuration
            </CardDescription>
          </CardHeader>
          <CardContent>
            {itemsLoading ? (
              <div className="space-y-4">
                <Skeleton className="h-10 w-full" />
                <Skeleton className="h-10 w-full" />
              </div>
            ) : itemsInfo ? (
              <div className="space-y-6">
                <div>
                  <h4 className="text-sm font-semibold uppercase tracking-wider text-muted-foreground mb-3">Item Rates</h4>
                  <div className="grid grid-cols-2 gap-3">
                    {itemsInfo.items.map(item => (
                      <div key={item.type} className="bg-secondary p-3 rounded-lg flex justify-between items-center">
                        <span className="font-medium">{item.type}</span>
                        <span className="font-mono font-bold text-primary">{item.pointsPerItem} CP</span>
                      </div>
                    ))}
                  </div>
                </div>
                
                <div className="pt-4 border-t">
                  <div className="flex justify-between items-center">
                    <span className="text-sm font-medium">New User Welcome Bonus</span>
                    <span className="bg-primary/10 text-primary px-3 py-1 rounded-full font-bold text-sm font-mono">
                      +{itemsInfo.welcomeBonus} CP
                    </span>
                  </div>
                </div>
              </div>
            ) : (
              <div className="p-4 text-center text-muted-foreground">
                Configuration unavailable.
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </>
  );
}

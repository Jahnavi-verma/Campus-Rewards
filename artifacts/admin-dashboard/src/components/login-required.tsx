import { AlertCircle, LogIn } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";

interface LoginRequiredProps {
  title?: string;
  description?: string;
}

export function LoginRequired({ 
  title = "Authentication Required", 
  description = "You need to be logged in as an administrator to view this data." 
}: LoginRequiredProps) {
  return (
    <Card className="max-w-md mx-auto mt-12 border-destructive/20 bg-destructive/5">
      <CardHeader className="text-center pb-4">
        <div className="mx-auto bg-destructive/10 w-12 h-12 rounded-full flex items-center justify-center mb-4 text-destructive">
          <AlertCircle className="w-6 h-6" />
        </div>
        <CardTitle className="text-xl">{title}</CardTitle>
        <CardDescription className="text-base mt-2">
          {description}
        </CardDescription>
      </CardHeader>
      <CardContent className="flex justify-center pb-8">
        <Button asChild size="lg" className="gap-2">
          <a href="https://a10ee76d-c027-4b36-a368-defbd77db51b-00-3qfg14ptdj2xd.sisko.replit.dev/api/oauth2/authorization/github">
            <LogIn className="w-4 h-4" />
            Login with GitHub
          </a>
        </Button>
      </CardContent>
    </Card>
  );
}
